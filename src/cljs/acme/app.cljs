(ns acme.app
  (:require [acme.i18n :as i18n]
            [acme.test :as test]
            [reagent.core :as r]
            [reagent.dom :as rdom]
            ))

(defonce route (r/atom :home))

(defn root []
  [:div
   [:div
    [:input {:type "button" :value (i18n/translate :hello)}]
    [:input {:type "button" :value "Hello World" :on-click #(reset! route :test)}]
    [:input {:type "button" :value "English" :on-click #(i18n/switch-lang :en)}]
    [:input {:type "button" :value "Spanish" :on-click #(i18n/switch-lang :el)}]
    [:input {:type "button" :value "German" :on-click #(i18n/switch-lang :de)}]]
   [:div
    (cond (= @route :home)    [:h1 (i18n/translate :hello)]
          (= @route :test)    [test/hello]
          (= @route :map)     [:h1 (i18n/translate :map)]
          (= @route :library) [:h1 (i18n/translate :library)]
          (= @route :home)    [:h1 (i18n/translate :home)]
          :else               [:h1 (i18n/translate :home)])]])

(defn ^:export init []
  (rdom/render [root] (js/document.getElementById "root")))

(i18n/compile-logs)
