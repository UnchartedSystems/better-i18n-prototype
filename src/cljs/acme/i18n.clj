(ns acme.i18n
  (:require [cljs.analyzer :refer [*cljs-ns*]]
            [clojure.java.io :refer [resource reader]]
            [clojure.set :refer [difference]]))

;; -------------
;; Creating Logs
;; -------------

;; TODO: integrate with build tool to watch translations.edn
(def translations (read-string (slurp "translations.edn")))

(def logs (atom []))
#_(def logs-compiled? (atom false))

(defn add-log [log]
  (swap! logs conj log))

(def all-phrases (into #{} (keys translations)))
(def used-phrases (atom #{}))

(defmacro translate [phrase]
  (if (contains? all-phrases phrase)
    (swap! used-phrases conj phrase)
    (add-log {:type     :no-phrase
              :info   phrase
              :metadata (merge {:ns (name *cljs-ns*)} (meta &form))}))
  `(@current-lang ~(phrase translations)))

(def all-langs (reduce #(into %1 (keys (second %2))) #{} translations))
(def used-langs (atom #{}))

(defmacro switch-lang [lang]
  (if (contains? all-langs lang)
    (swap! used-langs conj lang)
    (add-log {:type     :no-lang
              :info     lang
              :metadata (merge {:ns (name *cljs-ns*)} (meta &form))}))
  `(reset! current-lang ~lang))


;; -------------
;; Printing Logs
;; -------------

(defn pad-digits [nums]
  (let [len?    #(count (str %))
        max-len (apply max (mapv len? nums))]
    (mapv #(str (apply str (repeat (- max-len (len? %)) " ")) %) nums)))

;; TODO: - Account for file-lines offset from 0-based numbering
(defn print-code [{:keys [line end-line] path :file}]
  (let [file-lines (line-seq (reader (.getPath (resource path))))
        padding    2
        start-line (max 0 (- (dec line) padding))
        end-line   (min (count file-lines) (+ end-line padding))
        lines-nums (pad-digits (range start-line end-line))]
    (->> file-lines
         (drop start-line)
         (take (- end-line start-line))
         (mapv println (repeat " ") lines-nums (repeat "|")))))

(defn print-log [{:keys [type info metadata]}]
  (let [spacer (apply str (take 80 (repeat "-")))
        error (cond (= :no-phrase type) "Unsupported Phrase"
                    (= :no-lang type)   "Unsupported Language")]
    (println spacer)
    (println "  ERROR: " error "-" info)
    (println "  FILE:  " (.getPath (resource (:file metadata))))
    (println spacer)
    (print-code metadata)
    (println spacer "\n")))

;; --------------
;; Compiling Logs
;; --------------

;; TODO: langs coverage is substandard - will improve
;;        - difficult to parse & extend
;;        - doesn't show missing phrases when under 100% coverage
;;        - doesn't log error when used langs are missing from used phrases translations
(defmacro compile-logs []
  (let [langs-coverage (reduce (fn [acc lang] (assoc acc lang (reduce #(if (lang %2) (inc %1) %1) 0 (vals translations)))) {} all-langs)
        unused-phrases (difference all-phrases @used-phrases)
        unused-langs   (difference all-langs @used-langs)]
    (println "\nTranslation Logs:" )
    (apply println "Lang coverage: " (mapv (fn [[l n]] (str l ": " n "/" (count all-phrases))) (seq langs-coverage)))
    (when (not-empty unused-phrases) (apply println "Translations include unused Phrases: " unused-phrases))
    (when (not-empty unused-langs) (apply println "Translations include unused Langs:   " unused-langs))
    (mapv print-log @logs)
    (reset! logs nil)))
