/*
 * Copyright 2023 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.testreport

import sbt.{Def, *}
import scalatags.Text.all.*

object TestReportPlugin extends AutoPlugin {

  override def trigger = allRequirements

  object autoImport {
    val testReport = taskKey[Unit]("generate test report")
    val testReportDirectory = settingKey[File]("test report directory")
  }

  import autoImport.*

  override lazy val projectSettings: Seq[Def.Setting[?]] = Seq(
    testReport := generateTestReport().value,
    testReportDirectory := Keys.target.value / "test-reports"
  )

  private def generateTestReport(): Def.Initialize[Task[Unit]] = Def.task {
    val axeResultsDirectory = os.Path(testReportDirectory.value / "accessibility-assessment" / "axe-results")
    val logger = sbt.Keys.streams.value.log

    if (os.exists(axeResultsDirectory)) {
      logger.info("Generating accessibility assessment report ...")

      val projectName = Keys.name.value
      val isJenkinsBuild = sys.env.contains("BUILD_ID")
      val jenkinsBuildId = sys.env.get("BUILD_ID")
      val jenkinsBuildUrl = sys.env.getOrElse("BUILD_URL", "#")
      val htmlReportDirectory = testReportDirectory.value / "accessibility-assessment" / "html-report"

      os.makeDir.all(os.Path(htmlReportDirectory))

      val axeResults = os.list.stream(axeResultsDirectory).filter(os.isDir).map { timestampDirectory =>
        ujson.read(os.read(timestampDirectory / "axeResults.json"))
      }

      os.write.over(
        os.Path(htmlReportDirectory / "index.html"),
        "<!DOCTYPE html>" + html(
          head(
            meta(charset := "utf-8"),
            meta(name := "viewport", content := "width=device-width, initial-scale=1"),
            tag("title")(s"Accessibility assessment for $projectName"),
            tag("style")(
              """*,:after,:before{box-sizing:border-box}blockquote,body,dd,dl,figure,h1,h2,h3,h4,p{margin:0}ol[role=list],ul[role=list]{list-style:none}html{text-size-adjust:none;-webkit-text-size-adjust:none}html:focus-within{scroll-behavior:smooth}body{line-height:1.5;min-height:100vh;text-rendering:optimizeSpeed}a:not([class]){text-decoration-skip-ink:auto}img,picture,svg{display:block;height:auto;max-width:100%}button,input,select,textarea{font:inherit}@font-face{font-display:swap;font-family:Inter;font-style:normal;font-weight:400;src:url(/assets/fonts/inter-v12-latin-regular.woff2) format("woff2")}@font-face{font-display:swap;font-family:Inter;font-style:normal;font-weight:700;src:url(/assets/fonts/inter-v12-latin-700.woff2) format("woff2")}:root{--color-red:#d4351c;--color-yellow:#ffdd00;--color-green:#00703c;--color-blue:#1d70b8;--color-black:#0b0c0c;--color-dark-grey:#505a5f;--color-mid-grey:#b1b4b6;--color-light-grey:#f3f2f1;--color-white:#ffffff;--font-base:"Inter",sans-serif;--size-step-0:clamp(1rem, calc(0.96rem + 0.22vw), 1.13rem);--size-step-1:clamp(1.25rem, calc(1.16rem + 0.43vw), 1.5rem);--size-step-2:clamp(1.56rem, calc(1.41rem + 0.76vw), 2rem);--size-step-3:clamp(1.95rem, calc(1.71rem + 1.24vw), 2.66rem);--size-step-4:clamp(2.44rem, calc(2.05rem + 1.93vw), 3.55rem);--size-step-5:clamp(3.05rem, calc(2.47rem + 2.93vw), 4.74rem);accent-color:var(--color-blue)}body{background:var(--color-white);color:var(--color-black);display:flex;flex-direction:column;font-family:var(--font-base);font-size:var(--size-step-0);font-weight:400;line-height:1.8}main{flex:auto}a{color:var(--color-blue)}a:hover{text-decoration-thickness:.3ex}h1,h2,h3{font-weight:700;line-height:1.2}h1{font-size:var(--size-step-3)}h2{font-size:var(--size-step-2)}h3{font-size:var(--size-step-1)}nav>ul{margin:0}[role=list]{margin-bottom:0;padding:0}:focus,:focus-visible{outline:3px solid var(--color-yellow);outline-offset:0}main:focus{outline:0}::selection{background:var(--color-yellow);color:var(--color-black)}.js-enabled .js-hidden{display:none}.banner{--region-space:var(--size-step-0);--wrapper-space:var(--size-step-0);background-color:var(--color-blue);color:var(--color-white);text-align:center}.banner a{color:currentColor}.brand{height:var(--size-step-3);width:var(--size-step-3)}form{--flow-space:var(--size-step-3)}label,legend{display:block;font-size:var(--size-step-2);font-weight:700}label[for*=impact]{font-size:var(--size-step-0);font-weight:400}fieldset{--cluster-space:0.75rem;min-width:0;margin:0;padding:0;border:0}fieldset ul{--flow-space:var(--size-step-0)}input[type=checkbox]{height:var(--size-step-1);width:var(--size-step-1)}input[type=text]{--flow-space:var(--size-step-0);border:2px solid var(--color-black);line-height:1;padding:1ex;width:100%}button{all:unset;background-color:var(--color-green);box-shadow:0 2px #002d18;color:var(--color-white);cursor:pointer;display:inline-block;line-height:1;padding:1ex;text-align:center;width:100%}button:focus:not(:active):not(:hover){background-color:var(--color-yellow);color:var(--color-black);outline:3px solid transparent}button:hover{background-color:#005a30}.summary{--border-color:var(--color-mid-grey);--region-space:var(--size-step-0);--wrapper-space:var(--size-step-0)}.summary header{background-color:var(--color-light-grey)}.summary header h2{font-size:var(--size-step-0);text-transform:uppercase}.summary header ul{--cluster-space:0.5rem}.summary dt{font-weight:700}.summary dd ul{list-style-type:disc}.summary pre{background-color:var(--color-light-grey);margin:0;padding:1.5ex;white-space:pre-wrap}.tag{display:inline-block;font-size:1rem;font-weight:700;letter-spacing:1px;line-height:1;padding:.75ex 1ex;text-transform:uppercase}.tag-blue,[data-tag=info]{background:#d2e2f1;color:#144e81}.tag-green{background:#cce2d8;color:#005a30}.tag-red,[data-tag=critical],[data-tag=serious]{background-color:#f6d7d2;color:#942514}.tag-yellow,[data-tag=moderate]{background:#fff7bf;color:#594d00}[data-tag=version]{background-color:var(--color-blue);color:var(--color-white)}.cluster{align-items:var(--cluster-vertical-alignment,center);display:flex;flex-wrap:var(--cluster-flex-wrap,wrap);gap:var(--cluster-space,var(--size-step-2));justify-content:var(--cluster-horizontal-alignment,flex-start)}.repel{align-items:var(--repel-vertical-alignment,center);display:flex;flex-wrap:var(--repel-flex-wrap,wrap);gap:var(--repel-space,var(--size-step-2));justify-content:var(--cluster-horizontal-alignment,space-between)}.sidebar{display:flex;flex-wrap:wrap;gap:var(--sidebar-space,var(--size-step-5))}.sidebar>:first-child{flex-grow:1}.sidebar>:last-child{flex-basis:0;flex-grow:999;min-inline-size:50%}.border{border-color:var(--border-color,var(--color-light-grey));border-style:var(--border-style,solid);border-width:var(--border-width,2px)}.border-bottom{border-bottom-color:var(--border-color,var(--color-light-grey));border-bottom-style:var(--border-style,solid);border-bottom-width:var(--border-width,2px)}.border-top{border-top-color:var(--border-color,var(--color-light-grey));border-top-style:var(--border-style,solid);border-top-width:var(--border-width,2px)}.flow>*+*{margin-block-start:var(--flow-space,var(--size-step-0))}.region{padding-block:var(--region-space,var(--size-step-3))}.visually-hidden{border:0;clip:rect(0 0 0 0);height:auto;margin:0;overflow:hidden;padding:0;position:absolute;width:1px;white-space:nowrap}.wrapper{padding-inline:var(--wrapper-space,var(--size-step-3));width:100%}"""
            ),
            if (axeResults.count() >= 0) meta(name := "description", content := s"${axeResults.count()} issues identified.")
            else meta(name := "description", content := "No issues identified."),
//            meta(property := "og:title", content := s"Accessibility assessment for $projectName"),
//            meta(property := "og:url", content := "https://build.tax.service.gov.uk/"),
//            meta(property := "og:image", content := "/assets/images/share.png"),
//            meta(property := "og:type", content := "website"),
//            if (axeResults.count() >= 0) meta(property := "og:description", content := s"${axeResults.count()} issues identified.")
//            else meta(property := "og:description", content := "No issues identified."),
//            meta(property := "og:locale", content := "en_GB"),
//            link(rel := "canonical", href := "https://build.tax.service.gov.uk/"),
//            link(rel := "icon", href := "/favicon.ico", sizes := "any"),
//            link(rel := "icon", href := "/assets/images/favicon.svg", tpe := "image/svg+xml"),
//            link(rel := "apple-touch-icon", href := "/assets/images/apple-touch-icon.png"),
//            link(rel := "manifest", href := "/manifest.webmanifest"),
//            meta(name := "theme-color", content := "#1d70b8")
            ),
          body(
            script("document.body.className += ' js-enabled';"),
            header(
              cls := "border-bottom",
              role := "banner",
              div(
                cls := "banner region wrapper",
                p(
                  "Generated from ",
                  if (isJenkinsBuild) a(href := jenkinsBuildUrl, jenkinsBuildId)
                  else "local build",
                  " (BROWSER) of ",
                  a(href := s"https://github.com/hmrc/$projectName", projectName),
                  " on ",
//                  <time datetime="2023-09-02T15:29:49.532+01:00">2 September 2023 at 15:29 BST</time>
                )
              )
            ),
            footer(),


//              <header class="border-bottom" role="banner">
//                <div class="banner region wrapper">
//                  <p>Generated from build
//                    <a href="#">#123</a>
//                    (Chrome) of
//                    <a href="#">service-name-ui-journey-tests</a>
//                    on
//                    <time datetime="2023-09-02T15:29:49.532+01:00">2 September 2023 at 15:29 BST</time>
//                  </p>
//                </div>
//                <div class="repel region wrapper">
//                  <a class="brand" href="/" aria-label="Accessibility assessment">
//                    <svg width="100" height="100" viewBox="0 0 100 100" fill="currentColor" xmlns="http://www.w3.org/2000/svg">
//                      <path
//                      d="M50,0 C77.6142375,0 100,22.3857625 100,50 C100,77.6142375 77.6142375,100 50,100 C22.3857625,100 0,77.6142375 0,50 C0,22.3857625 22.3857625,0 50,0 Z M50,9.1796875 C27.4555639,9.1796875 9.1796875,27.4555639 9.1796875,50 C9.1796875,72.5444361 27.4555639,90.8203125 50,90.8203125 C72.5444361,90.8203125 90.8203125,72.5444361 90.8203125,50 C90.8203125,27.4555639 72.5444361,9.1796875 50,9.1796875 Z M50,16.9921875 C68.2297115,16.9921875 83.0078125,31.7702885 83.0078125,50 C83.0078125,68.2297115 68.2297115,83.0078125 50,83.0078125 C31.7702885,83.0078125 16.9921875,68.2297115 16.9921875,50 C16.9921875,31.7702885 31.7702885,16.9921875 50,16.9921875 Z M66.796875,39.453125 L33.203125,39.453125 C31.0457627,39.453125 29.296875,41.2020127 29.296875,43.359375 C29.296875,45.5167373 31.0457627,47.265625 33.203125,47.265625 L46.2890625,47.265625 L46.2890625,55.0792969 L35.2174276,69.250252 C33.8892228,70.9502767 34.1906423,73.4051418 35.890667,74.7333467 C37.5906917,76.0615515 40.0455568,75.760132 41.3737617,74.0601073 L50.1,62.890625 L50.234375,62.890625 L58.9608158,74.0601073 C60.2890207,75.760132 62.7438858,76.0615515 64.4439105,74.7333467 C66.1439352,73.4051418 66.4453547,70.9502767 65.1171498,69.250252 L54.1015625,55.1511719 L54.1015625,47.265625 L66.796875,47.265625 C68.9542373,47.265625 70.703125,45.5167373 70.703125,43.359375 C70.703125,41.2020127 68.9542373,39.453125 66.796875,39.453125 Z M50.0976562,23.828125 C46.8076787,23.828125 44.140625,26.4951787 44.140625,29.7851562 C44.140625,33.0751338 46.8076787,35.7421875 50.0976562,35.7421875 C53.3876338,35.7421875 56.0546875,33.0751338 56.0546875,29.7851562 C56.0546875,26.4951787 53.3876338,23.828125 50.0976562,23.828125 Z"></path>
//                    </svg>
//                  </a>
//                  <nav aria-label="primary navigation">
//                    <ul class="cluster" role="list">
//                      <li>
//                        <a href="#" target="_blank">Guidance</a>
//                      </li>
//                      <li>
//                        <a href="#" target="_blank">Support</a>
//                      </li>
//                      <li>
//                        <a href="mailto:email@example.com?subject=Feedback: Accessibility assessment
//      & body=Build job #123" target="_blank">
//      Feedback
//      </a> </li>
//      </ul>
//      </nav>
//      </div>
//      </header>
//      <main id="issues" class="sidebar region wrapper">
//        <aside id="sidebar" class="js-hidden">
//          <form id="form" class="flow">
//            <label for="keywords">Search</label> <input id="search" class="search" type="text" name="search"/>
//            <fieldset class="flow">
//              <legend>Impact</legend>
//              <ul class="flow" role="list" aria-live="assertive">
//                <li class="cluster">
//                  <input id="impact-critical" type="checkbox" name="impact" value="critical"/> <label for="impact-critical">Critical</label>
//                </li>
//                <li class="cluster">
//                  <input id="impact-serious" type="checkbox" name="impact" value="serious"/> <label for="impact-serious">Serious</label>
//                </li>
//                <li class="cluster">
//                  <input id="impact-moderate" type="checkbox" name="impact" value="moderate"/> <label for="impact-moderate">Moderate</label>
//                </li>
//                <li class="cluster">
//                  <input id="impact-info" type="checkbox" name="impact" value="info"/> <label for="impact-info">Info</label>
//                </li>
//              </ul>
//            </fieldset>
//            <button id="clear">Clear</button>
//          </form>
//        </aside>
//        <article class="flow">
//          <h1>Accessibility assessment</h1>
//          <p aria-live="assertive" id="issueCount">Displaying all issues identified.</p>
//          <ul class="flow list" role="list">
//            <li data-impact="critical" data-hash="ABC123">
//              <article class="summary border">
//                <header class="repel border-bottom region wrapper">
//                  <h2>
//                    <a class="id" href="https://dequeuniversity.com/rules/axe/4.7/region?application=axeAPI" target="_blank">region</a>
//                  </h2>
//                  <ul class="cluster" role="list">
//                    <li>
//                      <span class="impact tag" data-tag="critical">critical</span>
//                    </li>
//                    <li>
//                      <span class="version tag" data-tag="version">4.7.2</span>
//                    </li>
//                  </ul>
//                </header>
//                <div class="wrapper">
//                  <dl>
//                    <div class="border-bottom region">
//                      <dt>Help</dt>
//                      <dd class="help">All page content should be contained by landmarks</dd>
//                    </div>
//                    <div class="border-bottom region">
//                      <dt>HTML</dt>
//                      <dd>
//                        <pre class="html">
//                          &lt;
//                          a href=
//                          &quot;
//                          #main-content
//                          &quot;
//                          class=
//                          &quot;
//                          govuk-skip-link
//                          &quot;
//                          data-module=
//                          &quot;
//                          govuk-skip-link
//                          &quot; &gt;
//                          Skip to main content
//                          &lt;
//                          /a
//                          &gt;
//                        </pre>
//                      </dd>
//                    </div>
//                    <div class="border-bottom region">
//                      <dt>Affects</dt>
//                      <dd>
//                        <details>
//                          <summary>2 URLs</summary>
//                          <ul class="affects">
//                            <li>
//                              <a href="#">http://localhost:9080/check-your-vat-flat-rate/vat-return-period</a>
//                            </li>
//                            <li>
//                              <a href="#">http://localhost:9080/check-your-vat-flat-rate/turnover</a>
//                            </li>
//                          </ul>
//                        </details>
//                      </dd>
//                    </div>
//                    <div class="region">
//                      <dt>Permalink</dt>
//                      <dd>
//                        <a class="permalink" href="http://localhost:8080/?search=ABC123">http://localhost:8080/?search=ABC123</a>
//                      </dd>
//                    </div>
//                  </dl>
//                </div>
//              </article>
//            </li>
//          </ul>
//        </article>
//      </main>

            footer(
              cls := "border-top repel region wrapper",
              role := "contentinfo",
              p("&copy; 2023 ", projectName),
              tag("nav")(
                attr("aria-label") := "secondary navigation",
                ul(
                  li(
                    a(href := "#", "Back to top")
                  )
                )
              )
            ),
            script("""var List=function(r){function n(t){var e;return(i[t]||(e=i[t]={i:t,l:!1,exports:{}},r[t].call(e.exports,e,e.exports,n),e.l=!0,e)).exports}var i={};return n.m=r,n.c=i,n.i=function(t){return t},n.d=function(t,e,r){n.o(t,e)||Object.defineProperty(t,e,{configurable:!1,enumerable:!0,get:r})},n.n=function(t){var e=t&&t.__esModule?function(){return t.default}:function(){return t};return n.d(e,"a",e),e},n.o=function(t,e){return Object.prototype.hasOwnProperty.call(t,e)},n.p="",n(n.s=11)}([function(t,e,r){function n(t){if(!t||!t.nodeType)throw new Error("A DOM element reference is required");this.el=t,this.list=t.classList}var i=r(4),s=/\s+/;Object.prototype.toString,t.exports=function(t){return new n(t)},n.prototype.add=function(t){var e;return this.list?this.list.add(t):(e=this.array(),~i(e,t)||e.push(t),this.el.className=e.join(" ")),this},n.prototype.remove=function(t){var e;return this.list?this.list.remove(t):(e=this.array(),~(t=i(e,t))&&e.splice(t,1),this.el.className=e.join(" ")),this},n.prototype.toggle=function(t,e){return this.list?void 0!==e&&e===this.list.toggle(t,e)||this.list.toggle(t):void 0!==e?e?this.add(t):this.remove(t):this.has(t)?this.remove(t):this.add(t),this},n.prototype.array=function(){var t=(this.el.getAttribute("class")||"").replace(/^\s+|\s+$/g,"").split(s);return""===t[0]&&t.shift(),t},n.prototype.has=n.prototype.contains=function(t){return this.list?this.list.contains(t):!!~i(this.array(),t)}},function(t,e,r){var s=window.addEventListener?"addEventListener":"attachEvent",a=window.removeEventListener?"removeEventListener":"detachEvent",o="addEventListener"!=s?"on":"",l=r(5);e.bind=function(t,e,r,n){t=l(t);for(var i=0;i<t.length;i++)t[i][s](o+e,r,n||!1)},e.unbind=function(t,e,r,n){t=l(t);for(var i=0;i<t.length;i++)t[i][a](o+e,r,n||!1)}},function(t,e){t.exports=function(i){return function(t,e,r){var n=this;this._values={},this.found=!1,this.filtered=!1;this.values=function(t,e){if(void 0===t)return n._values;for(var r in t)n._values[r]=t[r];!0!==e&&i.templater.set(n,n.values())},this.show=function(){i.templater.show(n)},this.hide=function(){i.templater.hide(n)},this.matching=function(){return i.filtered&&i.searched&&n.found&&n.filtered||i.filtered&&!i.searched&&n.filtered||!i.filtered&&i.searched&&n.found||!i.filtered&&!i.searched},this.visible=function(){return!(!n.elm||n.elm.parentNode!=i.list)},t=t,r=r,void 0===(e=e)?r?n.values(t,r):n.values(t):(n.elm=e,r=i.templater.get(n,t),n.values(r))}}},function(t,e){t.exports=function(t,e,r,n){{if((n=n||{}).test&&n.getElementsByClassName||!n.test&&document.getElementsByClassName)return f=t,h=e,r?f.getElementsByClassName(h)[0]:f.getElementsByClassName(h);if(n.test&&n.querySelector||!n.test&&document.querySelector)return f=t,h="."+(h=e),r?f.querySelector(h):f.querySelectorAll(h);n=t,t=e;for(var i=r,s=[],a=n.getElementsByTagName("*"),o=a.length,l=new RegExp("(^|\\s)"+t+"(\\s|$)"),u=0,c=0;u<o;u++)if(l.test(a[u].className)){if(i)return a[u];s[c]=a[u],c++}return s}var f,h}},function(t,e){var n=[].indexOf;t.exports=function(t,e){if(n)return t.indexOf(e);for(var r=0;r<t.length;++r)if(t[r]===e)return r;return-1}},function(t,e){t.exports=function(t){if(void 0===t)return[];if(null===t)return[null];if(t===window)return[window];if("string"==typeof t)return[t];if("[object Array]"===Object.prototype.toString.call(t))return t;if("number"!=typeof t.length)return[t];if("function"==typeof t&&t instanceof Function)return[t];for(var e=[],r=0;r<t.length;r++)(Object.prototype.hasOwnProperty.call(t,r)||r in t)&&e.push(t[r]);return e.length?e:[]}},function(t,e){t.exports=function(t){return(t=null===(t=void 0===t?"":t)?"":t).toString()}},function(t,e){t.exports=function(t){for(var e,r=Array.prototype.slice.call(arguments,1),n=0;e=r[n];n++)if(e)for(var i in e)t[i]=e[i];return t}},function(t,e){t.exports=function(i){function s(t,e,r){var n=t.splice(0,50);r=(r=r||[]).concat(i.add(n)),0<t.length?setTimeout(function(){s(t,e,r)},1):(i.update(),e(r))}return s}},function(t,e){t.exports=function(s){return s.handlers.filterStart=s.handlers.filterStart||[],s.handlers.filterComplete=s.handlers.filterComplete||[],function(t){if(s.trigger("filterStart"),s.i=1,s.reset.filter(),void 0===t)s.filtered=!1;else{s.filtered=!0;for(var e=s.items,r=0,n=e.length;r<n;r++){var i=e[r];t(i)?i.filtered=!0:i.filtered=!1}}return s.update(),s.trigger("filterComplete"),s.visibleItems}}},function(t,e,r){r(0);var n=r(1),i=r(7),o=r(6),u=r(3),c=r(19);t.exports=function(s,a){a=i({location:0,distance:100,threshold:.4,multiSearch:!0,searchClass:"fuzzy-search"},a=a||{});var l={search:function(t,e){for(var r=a.multiSearch?t.replace(/ +$/,"").split(/ +/):[t],n=0,i=s.items.length;n<i;n++)l.item(s.items[n],e,r)},item:function(t,e,r){for(var n=!0,i=0;i<r.length;i++){for(var s=!1,a=0,o=e.length;a<o;a++)l.values(t.values(),e[a],r[i])&&(s=!0);s||(n=!1)}t.found=n},values:function(t,e,r){if(t.hasOwnProperty(e)){t=o(t[e]).toLowerCase();if(c(t,r,a))return!0}return!1}};return n.bind(u(s.listContainer,a.searchClass),"keyup",function(t){t=t.target||t.srcElement;s.search(t.value,l.search)}),function(t,e){s.search(t,e,l.search)}}},function(t,e,s){var u=s(18),c=s(3),f=s(7),h=s(4),d=s(1),v=s(6),m=s(0),g=s(17),p=s(5);t.exports=function(t,r,e){var a=this,o=s(2)(a),l=s(8)(a),n=s(12)(a),i={start:function(){a.listClass="list",a.searchClass="search",a.sortClass="sort",a.page=1e4,a.i=1,a.items=[],a.visibleItems=[],a.matchingItems=[],a.searched=!1,a.filtered=!1,a.searchColumns=void 0,a.handlers={updated:[]},a.valueNames=[],a.utils={getByClass:c,extend:f,indexOf:h,events:d,toString:v,naturalSort:u,classes:m,getAttribute:g,toArray:p},a.utils.extend(a,r),a.listContainer="string"==typeof t?document.getElementById(t):t,a.listContainer&&(a.list=c(a.listContainer,a.listClass,!0),a.parse=s(13)(a),a.templater=s(16)(a),a.search=s(14)(a),a.filter=s(9)(a),a.sort=s(15)(a),a.fuzzySearch=s(10)(a,r.fuzzySearch),this.handlers(),this.items(),this.pagination(),a.update())},handlers:function(){for(var t in a.handlers)a[t]&&a.on(t,a[t])},items:function(){a.parse(a.list),void 0!==e&&a.add(e)},pagination:function(){if(void 0!==r.pagination){!0===r.pagination&&(r.pagination=[{}]),void 0===r.pagination[0]&&(r.pagination=[r.pagination]);for(var t=0,e=r.pagination.length;t<e;t++)n(r.pagination[t])}}};this.reIndex=function(){a.items=[],a.visibleItems=[],a.matchingItems=[],a.searched=!1,a.filtered=!1,a.parse(a.list)},this.toJSON=function(){for(var t=[],e=0,r=a.items.length;e<r;e++)t.push(a.items[e].values());return t},this.add=function(t,e){if(0!==t.length){if(!e){for(var r=[],n=0,i=(t=void 0===t[0]?[t]:t).length;n<i;n++){var s=a.items.length>a.page,s=new o(t[n],void 0,s);a.items.push(s),r.push(s)}return a.update(),r}l(t,e)}},this.show=function(t,e){return this.i=t,this.page=e,a.update(),a},this.remove=function(t,e,r){for(var n=0,i=0,s=a.items.length;i<s;i++)a.items[i].values()[t]==e&&(a.templater.remove(a.items[i],r),a.items.splice(i,1),s--,i--,n++);return a.update(),n},this.get=function(t,e){for(var r=[],n=0,i=a.items.length;n<i;n++){var s=a.items[n];s.values()[t]==e&&r.push(s)}return r},this.size=function(){return a.items.length},this.clear=function(){return a.templater.clear(),a.items=[],a},this.on=function(t,e){return a.handlers[t].push(e),a},this.off=function(t,e){t=a.handlers[t],e=h(t,e);return-1<e&&t.splice(e,1),a},this.trigger=function(t){for(var e=a.handlers[t].length;e--;)a.handlers[t][e](a);return a},this.reset={filter:function(){for(var t=a.items,e=t.length;e--;)t[e].filtered=!1;return a},search:function(){for(var t=a.items,e=t.length;e--;)t[e].found=!1;return a}},this.update=function(){var t=a.items,e=t.length;a.visibleItems=[],a.matchingItems=[],a.templater.clear();for(var r=0;r<e;r++)t[r].matching()&&a.matchingItems.length+1>=a.i&&a.visibleItems.length<a.page?(t[r].show(),a.visibleItems.push(t[r]),a.matchingItems.push(t[r])):(t[r].matching()&&a.matchingItems.push(t[r]),t[r].hide());return a.trigger("updated"),a},i.start()}},function(t,e,r){var m=r(0),g=r(1),n=r(11);t.exports=function(d){function r(t,e){var r,n=d.matchingItems.length,i=d.i,s=d.page,a=Math.ceil(n/s),o=Math.ceil(i/s),l=e.innerWindow||2,u=e.left||e.outerWindow||0,c=a-(e.right||e.outerWindow||0);t.clear();for(var f=1;f<=a;f++){var h=o===f?"active":"";v.number(f,u,c,o,l)?(r=t.add({page:f,dotted:!1})[0],h&&m(r.elm).add(h),function(t,e,r){g.bind(t,"click",function(){d.show((e-1)*r+1,r)})}(r.elm,f,s)):v.dotted(t,f,u,c,o,l,t.size())&&(r=t.add({page:"...",dotted:!0})[0],m(r.elm).add("disabled"))}}var v={number:function(t,e,r,n,i){return this.left(t,e)||this.right(t,r)||this.innerWindow(t,n,i)},left:function(t,e){return t<=e},right:function(t,e){return e<t},innerWindow:function(t,e,r){return e-r<=t&&t<=e+r},dotted:function(t,e,r,n,i,s,a){return this.dottedLeft(t,e,r,n,i,s)||this.dottedRight(t,e,r,n,i,s,a)},dottedLeft:function(t,e,r,n,i,s){return e==r+1&&!this.innerWindow(e,i,s)&&!this.right(e,n)},dottedRight:function(t,e,r,n,i,s,a){return!t.items[a-1].values().dotted&&e==n&&!this.innerWindow(e,i,s)&&!this.right(e,n)}};return function(t){var e=new n(d.listContainer.id,{listClass:t.paginationClass||"pagination",item:"<li><a class='page' href='javascript:function Z(){Z=\"\"}Z()'></a></li>",valueNames:["page","dotted"],searchClass:"pagination-search-that-is-not-supposed-to-exist",sortClass:"pagination-sort-that-is-not-supposed-to-exist"});d.on("updated",function(){r(e,t)}),r(e,t)}}},function(t,e,r){t.exports=function(i){function n(t,e){for(var r=0,n=t.length;r<n;r++)i.items.push(new a(e,t[r]))}function s(t,e){var r=t.splice(0,50);n(r,e),0<t.length?setTimeout(function(){s(t,e)},1):(i.update(),i.trigger("parseComplete"))}var a=r(2)(i);return i.handlers.parseComplete=i.handlers.parseComplete||[],function(){var t=function(t){for(var e=t.childNodes,r=[],n=0,i=e.length;n<i;n++)void 0===e[n].data&&r.push(e[n]);return r}(i.list),e=i.valueNames;(i.indexAsync?s:n)(t,e)}}},function(t,e){t.exports=function(r){function e(t){return r.trigger("searchStart"),o.resetList(),o.setSearchString(t),o.setOptions(arguments),o.setColumns(),""===s?l.reset():(r.searched=!0,a?a(s,i):l.list()),r.update(),r.trigger("searchComplete"),r.visibleItems}var n,i,s,a,o={resetList:function(){r.i=1,r.templater.clear(),a=void 0},setOptions:function(t){2==t.length&&t[1]instanceof Array?i=t[1]:2==t.length&&"function"==typeof t[1]?(i=void 0,a=t[1]):3==t.length?(i=t[1],a=t[2]):i=void 0},setColumns:function(){0!==r.items.length&&void 0===i&&(i=void 0===r.searchColumns?o.toArray(r.items[0].values()):r.searchColumns)},setSearchString:function(t){t=(t=r.utils.toString(t).toLowerCase()).replace(/[-[\]{}()*+?.,\\^$|#]/g,"\\$&"),s=t},toArray:function(t){var e,r=[];for(e in t)r.push(e);return r}},l={list:function(){for(var t=0,e=r.items.length;t<e;t++)l.item(r.items[t])},item:function(t){t.found=!1;for(var e=0,r=i.length;e<r;e++)if(l.values(t.values(),i[e]))return void(t.found=!0)},values:function(t,e){return!!(t.hasOwnProperty(e)&&(n=r.utils.toString(t[e]).toLowerCase(),""!==s)&&-1<n.search(s))},reset:function(){r.reset.search(),r.searched=!1}};return r.handlers.searchStart=r.handlers.searchStart||[],r.handlers.searchComplete=r.handlers.searchComplete||[],r.utils.events.bind(r.utils.getByClass(r.listContainer,r.searchClass),"keyup",function(t){t=t.target||t.srcElement;""===t.value&&!r.searched||e(t.value)}),r.utils.events.bind(r.utils.getByClass(r.listContainer,r.searchClass),"input",function(t){""===(t.target||t.srcElement).value&&e("")}),e}},function(t,e){t.exports=function(s){function t(){s.trigger("sortStart");var n={},t=arguments[0].currentTarget||arguments[0].srcElement||void 0,r=(t?(n.valueName=s.utils.getAttribute(t,"data-sort"),a.getInSensitive(t,n),n.order=a.getOrder(t)):((n=arguments[1]||n).valueName=arguments[0],n.order=n.order||"asc",n.insensitive=void 0===n.insensitive||n.insensitive),a.clear(),a.setOrder(n),n.sortFunction||s.sortFunction||null),i="desc"===n.order?-1:1;s.items.sort(r?function(t,e){return r(t,e,n)*i}:function(t,e){var r=s.utils.naturalSort;return r.alphabet=s.alphabet||n.alphabet||void 0,(r=!r.alphabet&&n.insensitive?s.utils.naturalSort.caseInsensitive:r)(t.values()[n.valueName],e.values()[n.valueName])*i}),s.update(),s.trigger("sortComplete")}var a={els:void 0,clear:function(){for(var t=0,e=a.els.length;t<e;t++)s.utils.classes(a.els[t]).remove("asc"),s.utils.classes(a.els[t]).remove("desc")},getOrder:function(t){var e=s.utils.getAttribute(t,"data-order");return"asc"==e||"desc"==e?e:!s.utils.classes(t).has("desc")&&s.utils.classes(t).has("asc")?"desc":"asc"},getInSensitive:function(t,e){t=s.utils.getAttribute(t,"data-insensitive");e.insensitive="false"!==t},setOrder:function(t){for(var e=0,r=a.els.length;e<r;e++){var n,i=a.els[e];s.utils.getAttribute(i,"data-sort")===t.valueName&&("asc"!=(n=s.utils.getAttribute(i,"data-order"))&&"desc"!=n||n==t.order)&&s.utils.classes(i).add(t.order)}}};return s.handlers.sortStart=s.handlers.sortStart||[],s.handlers.sortComplete=s.handlers.sortComplete||[],a.els=s.utils.getByClass(s.listContainer,s.sortClass),s.utils.events.bind(a.els,"click",t),s.on("searchStart",a.clear),s.on("filterStart",a.clear),t}},function(t,e){function r(l){var r,u=this;this.clearSourceItem=function(t,e){for(var r,n=0,i=e.length;n<i;n++){if(e[n].data)for(var s=0,a=e[n].data.length;s<a;s++)t.setAttribute("data-"+e[n].data[s],"");else e[n].attr&&e[n].name?(r=l.utils.getByClass(t,e[n].name,!0))&&r.setAttribute(e[n].attr,""):(r=l.utils.getByClass(t,e[n],!0))&&(r.innerHTML="");r=void 0}return t},this.getItemSource=function(t){var e;if(void 0!==t)return/<tr[\s>]/g.exec(t)?((e=document.createElement("tbody")).innerHTML=t,e.firstChild):-1!==t.indexOf("<")?((e=document.createElement("div")).innerHTML=t,e.firstChild):document.getElementById(l.item)||void 0;for(var r=l.list.childNodes,n=0,i=r.length;n<i;n++)if(void 0===r[n].data)return r[n].cloneNode(!0)},this.get=function(t,e){u.create(t);for(var r,n={},i=0,s=e.length;i<s;i++){if(e[i].data)for(var a=0,o=e[i].data.length;a<o;a++)n[e[i].data[a]]=l.utils.getAttribute(t.elm,"data-"+e[i].data[a]);else e[i].attr&&e[i].name?(r=l.utils.getByClass(t.elm,e[i].name,!0),n[e[i].name]=r?l.utils.getAttribute(r,e[i].attr):""):(r=l.utils.getByClass(t.elm,e[i],!0),n[e[i]]=r?r.innerHTML:"");r=void 0}return n},this.set=function(t,e){var r,n,i=function(t){for(var e=0,r=l.valueNames.length;e<r;e++)if(l.valueNames[e].data){for(var n=l.valueNames[e].data,i=0,s=n.length;i<s;i++)if(n[i]===t)return{data:t}}else{if(l.valueNames[e].attr&&l.valueNames[e].name&&l.valueNames[e].name==t)return l.valueNames[e];if(l.valueNames[e]===t)return t}};if(!u.create(t))for(var s in e)e.hasOwnProperty(s)&&(r=s,s=e[s],n=void 0,r=i(r),r)&&(r.data?t.elm.setAttribute("data-"+r.data,s):r.attr&&r.name?(n=l.utils.getByClass(t.elm,r.name,!0))&&n.setAttribute(r.attr,s):(n=l.utils.getByClass(t.elm,r,!0))&&(n.innerHTML=s))},this.create=function(t){if(void 0!==t.elm)return!1;if(void 0===r)throw new Error("The list need to have at list one item on init otherwise you'll have to add a template.");var e=r.cloneNode(!0);return e.removeAttribute("id"),t.elm=e,u.set(t,t.values()),!0},this.remove=function(t){t.elm.parentNode===l.list&&l.list.removeChild(t.elm)},this.show=function(t){u.create(t),l.list.appendChild(t.elm)},this.hide=function(t){void 0!==t.elm&&t.elm.parentNode===l.list&&l.list.removeChild(t.elm)},this.clear=function(){if(l.list.hasChildNodes())for(;1<=l.list.childNodes.length;)l.list.removeChild(l.list.firstChild)},r=(r=u.getItemSource(l.item))&&u.clearSourceItem(r,l.valueNames)}t.exports=function(t){return new r(t)}},function(t,e){t.exports=function(t,e){var r=t.getAttribute&&t.getAttribute(e)||null;if(!r)for(var n=t.attributes.length,i=0;i<n;i++)void 0!==e[i]&&e[i].nodeName===e&&(r=e[i].nodeValue);return r}},function(t,e,r){"use strict";function d(t){return 48<=t&&t<=57}function n(t,e){for(var r=(t+="").length,n=(e+="").length,i=0,s=0;i<r&&s<n;){var a=t.charCodeAt(i),o=e.charCodeAt(s);if(d(a)){if(!d(o))return a-o;for(var l=i,u=s;48===a&&++l<r;)a=t.charCodeAt(l);for(;48===o&&++u<n;)o=e.charCodeAt(u);for(var c=l,f=u;c<r&&d(t.charCodeAt(c));)++c;for(;f<n&&d(e.charCodeAt(f));)++f;var h=c-l-f+u;if(h)return h;for(;l<c;)if(h=t.charCodeAt(l++)-e.charCodeAt(u++))return h;i=c,s=f}else{if(a!==o)return a<m&&o<m&&-1!==v[a]&&-1!==v[o]?v[a]-v[o]:a-o;++i,++s}}return r-n}var i,v,m=0;n.caseInsensitive=n.i=function(t,e){return n((""+t).toLowerCase(),(""+e).toLowerCase())},Object.defineProperties(n,{alphabet:{get:function(){return i},set:function(t){v=[];var e=0;if(i=t)for(;e<i.length;e++)v[i.charCodeAt(e)]=e;for(m=v.length,e=0;e<m;e++)void 0===v[e]&&(v[e]=-1)}}}),t.exports=n},function(t,e){t.exports=function(t,r,e){function n(t,e){t/=r.length,e=Math.abs(u-e);return s?t+e/s:e?1:t}var i=e.location||0,s=e.distance||100,e=e.threshold||.4;if(r===t)return!0;if(32<r.length)return!1;for(var a,o,l,u=i,c=function(){for(var t={},e=0;e<r.length;e++)t[r.charAt(e)]=0;for(e=0;e<r.length;e++)t[r.charAt(e)]|=1<<r.length-e-1;return t}(),f=e,h=(-1!=(d=t.indexOf(r,u))&&(f=Math.min(n(0,d),f),-1!=(d=t.lastIndexOf(r,u+r.length)))&&(f=Math.min(n(0,d),f)),1<<r.length-1),d=-1,v=r.length+t.length,m=0;m<r.length;m++){for(a=0,o=v;a<o;)n(m,u+o)<=f?a=o:v=o,o=Math.floor((v-a)/2+a);var v=o,g=Math.max(1,u-o+1),p=Math.min(u+o,t.length)+r.length,C=Array(p+2);C[p+1]=(1<<m)-1;for(var y=p;g<=y;y--){var b=c[t.charAt(y-1)];if(C[y]=0===m?(C[y+1]<<1|1)&b:(C[y+1]<<1|1)&b|(l[y+1]|l[y])<<1|1|l[y+1],C[y]&h){b=n(m,y-1);if(b<=f){if(f=b,!(u<(d=y-1)))break;g=Math.max(1,2*u-d)}}}if(n(m+1,u)>f)break;l=C}return!(d<0)}}])"""),
            script("""const issues=new List("issues",{valueNames:["id","impact","version","help","html","affects","permalink",{data:["hash"]}]});function updateUrl(){var e=new URL(window.location);e.search=new URLSearchParams(new FormData(form)),window.history.pushState({},"",e)}document.issues=issues,document.getElementById("sidebar").classList.remove("js-hidden");const issueCount=document.getElementById("issueCount");function displayIssueCount(){0===issues.items.length?issueCount.innerText="No issues identified.":issueCount.innerText=`Displaying ${issues.visibleItems.length} of ${issues.items.length} issues identified.`}const form=document.getElementById("form"),search=(form.addEventListener("submit",function(e){e.preventDefault()}),displayIssueCount(),document.getElementById("search")),filters=document.querySelectorAll("input[name='impact']");function applyFilters(){const s=Array.from(filters).reduce((e,s)=>e.concat(s.checked?s.value:[]),[]);s.length?issues.filter(e=>s.includes(e.values().impact)):issues.filter()}filters.forEach(e=>{e.addEventListener("input",applyFilters)});const clear=document.getElementById("clear");clear.addEventListener("click",()=>{search.value="",filters.forEach(e=>e.checked=!1),issues.search(),issues.filter()}),issues.on("updated",function(){displayIssueCount(),updateUrl()}),(initialSearchParams=new URLSearchParams(window.location.search)).forEach((s,e)=>{var t=document.querySelectorAll(`[name='${e}']`);"checkbox"===t[0].type?Array.from(t).forEach(e=>{e.value===s&&(e.checked=!0)}):t[0].value=s,"search"===e&&issues.search(s)}),""!==window.location.search&&applyFilters()""")
          )
        )
      )
    } else {
      logger.error("No axe results found to generate accessibility assessment report.")
    }
  }
}
