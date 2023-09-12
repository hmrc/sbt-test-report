async function init() {
    const issues = [];

    const {reportMetaData, axeAssessedPages} = reportData();

    // Init
    let initialSearchParams = new URLSearchParams(window.location.search);

    if(reportMetaData) {
        const reportMetaDataElement = document.getElementById('headerMetaData');
        const spanBuildNumber = document.createElement('span');
        const paragraph = document.createElement('p');
        if(reportMetaData.jenkinsBuildId) {
            paragraph.innerHTML = "Generated from build ";
            const arefBuildUrl = document.createElement('a');
            arefBuildUrl.href = reportMetaData.jenkinsBuildUrl;
            arefBuildUrl.innerText = reportMetaData.jenkinsBuildId;
            spanBuildNumber.appendChild(arefBuildUrl);
            paragraph.appendChild(spanBuildNumber);
            paragraph.innerHTML +=  ' (' + reportMetaData.browser + ')' + ' of '
        } else {
            paragraph.innerHTML = "Generated from";
        }

        const arefProjectName = document.createElement('a');
        arefProjectName.href = "https://github.com/hmrc/" + reportMetaData.projectName;
        arefProjectName.innerText = reportMetaData.projectName;
        paragraph.appendChild(arefProjectName);
        paragraph.innerHTML += ' on ';
        const timeStamp = document.createElement('time');
        timeStamp.dateTime = reportMetaData.dateOfAssessment;
        timeStamp.innerText = reportMetaData.dateOfAssessment;
        paragraph.appendChild(timeStamp);
        reportMetaDataElement.appendChild(paragraph);

        const footerProjectLinkElement = document.getElementById('footerProjectLink');
        footerProjectLinkElement.append(arefProjectName);
    }

    const violationList = document.getElementById('violations');
    const createViolations = () => {
        Array.from(axeAssessedPages).forEach(page => {
            const testEngineVersion = page.testEngine.version;
            Array.from(page.violations).forEach(violation => {
                const violationData = {
                    id: violation.id,
                    impact: violation.impact,
                    version: testEngineVersion,
                    help: violation.help,
                    html: Array.from(violation.nodes).map(node => node.html),
                    affects: page.url
                }
                const dataHash = MD5.generate(JSON.stringify(violationData));
                const windowHref = window.location.href;
                const permaLink = windowHref.indexOf('?search=') === -1 ?
                    windowHref + '?search=' + dataHash :
                    windowHref;
                console.log(permaLink);
                violationData.dataHash = dataHash;
                violationData.permaLink = permaLink;
                issues.push(violationData);

                let temp = document.getElementsByTagName("template")[0];
                let clonedTemplate = temp.content.cloneNode(true);
                const li = clonedTemplate.querySelector('li');

                li.setAttribute('data-hash', dataHash);
                li.setAttribute('data-impact', violationData.impact);

                const helpUrl = clonedTemplate.getElementById('helpUrl');
                helpUrl.setAttribute('href', violation.helpUrl);
                helpUrl.innerText = violationData.id;

                const impactTag = clonedTemplate.getElementById('impactTag');
                impactTag.setAttribute('data-tag', violationData.impact);
                impactTag.innerText = violationData.impact;

                const testEngine = clonedTemplate.getElementById('testEngineVersion');
                testEngine.innerText = testEngineVersion;

                const violationHelp = clonedTemplate.getElementById('violationHelp');
                violationHelp.innerText = violationData.help;

                const htmlSnippet = clonedTemplate.getElementById('htmlSnippet');
                htmlSnippet.innerText = violationData.html;

                const urlViolationSummary = clonedTemplate.getElementById('urlViolationSummary');
                urlViolationSummary.innerText = '1 URLs';
                const urlViolations = clonedTemplate.getElementById('urlViolations');
                const urlListItem = document.createElement('li');
                const urlHref = document.createElement('a');
                urlHref.innerText = violationData.affects;
                urlListItem.appendChild(urlHref);
                urlViolations.append(urlListItem);

                const violationPermaLink = clonedTemplate.getElementById('violationPermaLink');
                console.log('permaLink',permaLink);
                violationPermaLink.setAttribute('href', permaLink);
                violationPermaLink.innerText = permaLink;

                violationList.appendChild(clonedTemplate);
            });
        });
    }
    createViolations();

    const issueCount = document.getElementById('issueCount');
    function displayIssueCount() {
        const displayedViolations = document.querySelectorAll(`li[data-hash]`);

        const count = Array.from(displayedViolations).map(liViolation => {
            if(liViolation.style.display === 'none') {
                return 0;
            } else {
                return 1;
            }
        }).reduce((accumulator, currentValue) => {
            return accumulator + currentValue
        },0);

        if (count === 0) {
            issueCount.innerText = "No issues identified.";
        } else {
            issueCount.innerText = `Displaying ${count} of ${issues.length} issues identified.`;
        }
    }

    // Form
    const form = document.getElementById("form");
    form.addEventListener("submit", function (event) {
        event.preventDefault();
    });

    function updateUrl() {
        const url = new URL(window.location);
        const clearUrlParams = url.href.replace(url.search, '');

        history.pushState({}, "", clearUrlParams);
        window.location.href = clearUrlParams;
    }

    const searchViolations = (value) => {
        const violationsNotMatchingSearch = issues.filter(issue => {
            if(issue.id.indexOf(value) === -1 && issue.impact.indexOf(value) === -1 && issue.version.indexOf(value) === -1 &&
                issue.help.indexOf(value) === -1 && issue.html.indexOf(value) === -1 && issue.affects.indexOf(value) === -1 &&
                issue.dataHash.indexOf(value) === -1) {
                return issue;
            }
        });

        violationsNotMatchingSearch.forEach(violation => {
            const dataHashFound = document.querySelectorAll(`li[data-hash="${violation.dataHash}"]`);
            if(dataHashFound) {
                for (let i = 0; i < dataHashFound.length; ++i) {
                    dataHashFound[i].style.display = 'none';
                }
                displayIssueCount();
            }
        });
    }

    initialSearchParams.forEach((valueFromUrl, name) => {
        const fields = document.querySelectorAll(`[name='${name}']`);

        if (fields[0].type === "checkbox") {
            Array.from(fields).forEach((field) => {
                if (field.value === valueFromUrl) {
                    field.checked = true;
                }
            });
        } else {
            fields[0].value = valueFromUrl;
        }

        if (name === "search") {
            const foundIssue = issues.find(issue => issue.dataHash === valueFromUrl);
            console.log('foundIssue',foundIssue);
            if(foundIssue) {
                searchViolations(foundIssue.dataHash);
            }
        }
    });

    displayIssueCount();

    const clearSearch = () => {
        const dataHashFound = document.querySelectorAll(`li[data-hash]`);
        if(dataHashFound) {
            for (let i = 0; i < dataHashFound.length; ++i) {
                dataHashFound[i].style.display = 'block';
            }
            updateUrl();
            displayIssueCount();
        }
    }

    // Visibility (based on JavaScript enabled/disabled in browser)
    document.getElementById("sidebar").classList.remove("js-hidden");

    // Search
    const search = document.getElementById("search");

    // Clear
    const clear = document.getElementById("clear");

    clear.addEventListener("click", () => {
        search.value = "";
        filters.forEach((filter) => (filter.checked = false));
        clearSearch();
    });

    // Dynamic updates
    // issues.on("updated", function () {
    //     displayIssueCount();
    //     updateUrl();
    // });

    // URL query parameters


    // Filters
    function applyFilters() {
        const activeFilters = Array.from(filters).reduce((active, filter) => {
            return active.concat(filter.checked ? filter.value : []);
        }, []);

        if (activeFilters.length) {
            return issues.filter((issue) => {
                return activeFilters.includes(issue.impact);
            });
        }
    }

    const filters = document.querySelectorAll("input[name='impact']");
    filters.forEach((filter) => {
        filter.addEventListener("input", applyFilters);
    });

    // Remove "?search=" from start URL when search not applied
    console.log('window.location.search',window.location.search);
    if (window.location.search !== "") {
        applyFilters();
    }
}