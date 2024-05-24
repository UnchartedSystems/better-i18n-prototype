(ns acme.i18n
  (:require [cljs.analyzer :refer [*cljs-ns*]]
            [clojure.java.io :refer [file reader]]
            [clojure.set :refer [difference]]
            [shadow.resource :as rc]))

;; TODO: integrate with shadow to watch translations.edn
(def translations (read-string (slurp "translations.edn")))

; NOTE: proof of concept for printing error lines
;; (->> (line-seq (reader "src/main/compulsive/app.cljs"))
;;      (drop 21)
;;      (take 5)
;;      (map println '("  22 |" "  23 |" "  24 >" "  25 |" "  26 |")))

#_(println (meta #'translations))

;; NOTE proof of concept for getting absolute path
(->> (file "translations.edn")
     ( .getAbsolutePath))

(def logs (atom []))
(+ 1 1)
(defn add-log [log]
  (swap! logs conj log))


;; NOTE: used for error checking if logs already compiled
;; 'false' will be replaced with compile-logs metadata
#_(def logs-compiled? (atom false))

(def all-phrases (into #{} (keys translations)))

(def used-phrases (atom #{}))

;; TODO FIXME: Find out how to inline
;; https://clojureverse.org/t/using-none-code-resources-in-cljs-builds/3745

;; TODO: check if lang is key & collect log
(defmacro translate [phrase]
  #_(println (rc/slurp-resource &env "./translations.edn"))
  (if (contains? all-phrases phrase)
    (swap! used-phrases conj phrase)
    (add-log {:type     :no-phrase
              :metadata (merge {:ns (name *cljs-ns*)} (meta &form))
              :phrase   phrase}))
  `(@current-lang ~(phrase translations)))

(def all-langs (reduce #(into %1 (keys (second %2))) #{} translations))

(def used-langs (atom #{}))

;; TODO: check if lang is key & collect log
(defmacro switch-lang [lang]
  (if (contains? all-langs lang)
    (swap! used-langs conj lang)
    (add-log {:type     :no-lang
              :metadata (merge {:ns (name *cljs-ns*)} (meta &form))
              :lang     lang}))
  `(reset! current-lang ~lang))

(+ 1 1)

;; HACK: langs coverage is substandard and sloppy
;;        - difficult to parse & extend
;;        - doesn't show missing phrases when under 100% coverage
;;        - doesn't log error when used langs are missing from used phrases translations
(defmacro compile-logs []
  (let [langs-coverage (reduce (fn [acc lang] (assoc acc lang (reduce #(if (lang %2) (inc %1) %1) 0 (vals translations)))) {} all-langs)
        unused-phrases (difference all-phrases @used-phrases)
        unused-langs   (difference all-langs @used-langs)]
    (apply println "Lang coverage: " (mapv (fn [[l n]] (str l ": " n "/" (count all-phrases))) (seq langs-coverage)))
    (apply println "Translations include unused Phrases: " unused-phrases)
    (apply println "Translations include unused Langs:   " unused-langs)
    (mapv println @logs)))
