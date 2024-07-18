(ns acme.test
  (:require [acme.i18n :as i18n]))

(defn hello []
  [:div
   [:div
    [:input {:type "button" :value (i18n/translate :home)}]
    [:input {:type "button" :value (i18n/translate :hello)}]
    [:input {:type "button" :value (i18n/translate :wtf)}]
    [:input {:type "button" :value "English" :on-click #(i18n/switch-lang :en)}]
    [:input {:type "button" :value "Spanish" :on-click #(i18n/switch-lang :es)}]
    [:input {:type "button" :value "German" :on-click #(i18n/switch-lang :de)}]]])
