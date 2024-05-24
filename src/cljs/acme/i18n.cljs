(ns acme.i18n
  (:require-macros [acme.i18n])
  (:require [reagent.core :as r]))

;; NOTE: :private metadata is unenforced in cljs, but throws useful lsp errors.
(def ^:private current-lang (r/atom :es))
