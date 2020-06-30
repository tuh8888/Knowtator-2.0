(ns knowtator.views
  (:require
   [re-frame.core :as re-frame]
   [re-com.core :as re-com]
   [breaking-point.core :as bp]
   [knowtator.subs :as subs]
   [knowtator.events :as evts]
   [knowtator.html-util :as html]))

(defn home-title []
  [re-com/title
   :label "Knowtator"
   :level :level1])

(defn doc-header []
  [re-com/title
   :label @(re-frame/subscribe [::subs/visible-doc-id])
   :level :level2])

;; Editor

(defn popup-text-annotation
  [{:keys [ann content id]}]
  [re-com/p-span
   {:style {:background-color @(re-frame/subscribe [::subs/ann-color ann])
            :border           (when @(re-frame/subscribe [::subs/selected-span? id]) :solid)}}
   content])

(defn text-annotation
  [{:keys [content ann]}]
  [re-com/p-span {:style {:background-color @(re-frame/subscribe [::subs/ann-color ann])}}
   content])

(defn editor
  [doc-id]
  (let [paragraphs (re-frame/subscribe [::subs/highlighted-text])]
    [:div.text-annotation-editor {:on-click #(re-frame.core/dispatch [::evts/record-selection (html/text-selection (.-target %) "text-annotation-editor") doc-id])}
     (doall
       (for [paragraph @paragraphs]
         ^{:key (str (random-uuid))}
         [re-com/p
          {:style {:text-align   :justify
                   :text-justify :inter-word}}
          (doall
            (for [text paragraph]
              (if (string? text)
                text
                ^{:key (str (random-uuid))}
                [popup-text-annotation text])))]))]))

(defn document-controls
  []
  (let [selected-id (reagent.core/atom nil)]
    [re-com/h-box
     :children [[re-com/button
                 :label "add document"]
                [re-com/button
                 :label "remove document"]
                [re-com/single-dropdown
                 :choices @(re-frame/subscribe [::subs/doc-ids])
                 :model @(re-frame/subscribe [::subs/visible-doc-id])
                 :on-change #(re-frame/dispatch [::evts/select-doc %])]]]))

(defn home-panel
  []
  (let [doc-id (re-frame/subscribe [::subs/visible-doc-id])]
    [:div
     {:width @(re-frame/subscribe [::bp/screen-width])}
     [home-title]
     [document-controls]
     #_[annotation-controls]
     [doc-header]
     [editor @doc-id]]))

;; home

(defn display-re-pressed-example []
  (let [re-pressed-example (re-frame/subscribe [::subs/re-pressed-example])]
    [:div
     [:p
      [:span "Re-pressed is listening for keydown events. A message will be displayed when you type "]
      [:strong [:code "hello"]]
      [:span ". So go ahead, try it out!"]]

     (when-let [rpe @re-pressed-example]
       [re-com/alert-box
        :alert-type :info
        :body rpe])]))


(defn link-to-about-page []
  [re-com/hyperlink-href
   :label "go to About Page"
   :href "#/about"])

#_(defn home-panel []
    [re-com/v-box
     :gap "1em"
     :children [[home-title]
                [text-annotation]
                [link-to-about-page]
                [display-re-pressed-example]
                [:div
                 [:h3 (str "screen-width: " @(re-frame/subscribe [::bp/screen-width]))]
                 [:h3 (str "screen: " @(re-frame/subscribe [::bp/screen]))]]]])

;; about

(defn about-title []
  [re-com/title
   :label "This is the About Page."
   :level :level1])

(defn link-to-home-page []
  [re-com/hyperlink-href
   :label "go to Home Page"
   :href "#/"])

(defn about-panel []
  [re-com/v-box
   :gap "1em"
   :children [[about-title]
              [link-to-home-page]]])

;; main

(defn- panels [panel-name]
  (case panel-name
    :home-panel  [home-panel]
    :about-panel [about-panel]
    [:div]))

(defn show-panel [panel-name]
  [panels panel-name])

(defn main-panel []
  (let [active-panel (re-frame/subscribe [::subs/active-panel])]
    [re-com/v-box
     :height "100%"
     :children [[panels @active-panel]]]))
