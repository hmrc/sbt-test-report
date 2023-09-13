async function init() {
    const issues = [];

    const {reportMetaData, axeAssessedPages} = reportData();

    // Init
    let initialSearchParams = new URLSearchParams(window.location.search);

    // Create page header with report metadata
    if (reportMetaData) {
        const reportMetaDataElement = document.getElementById('headerMetaData');
        const spanBuildNumber = document.createElement('span');
        const paragraph = document.createElement('p');
        if (reportMetaData.jenkinsBuildId) {
            paragraph.innerHTML = "Generated from build ";
            const arefBuildUrl = document.createElement('a');
            arefBuildUrl.href = reportMetaData.jenkinsBuildUrl;
            arefBuildUrl.innerText = reportMetaData.jenkinsBuildId;
            spanBuildNumber.appendChild(arefBuildUrl);
            paragraph.appendChild(spanBuildNumber);

            let testEnvironment = "Chrome";
            if(axeAssessedPages && axeAssessedPages.length > 0 && axeAssessedPages[0].testEnvironment.userAgent.includes('edge')) {
                testEnvironment = "Edge"
            }
            paragraph.innerHTML += ' (' + testEnvironment + ')' + ' of '
        } else {
            paragraph.innerHTML = "Generated from ";
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

    const removeSearchFromUrlParams = () => {
        const url = new URL(window.location);
        let clearUrlParams = url.href;
        if (url.search !== "") {
            clearUrlParams = url.href.replace(url.search, '');
        }
        history.pushState({}, "", clearUrlParams);
        return clearUrlParams;
    }

    const clearSearchFromUrl = () => {
        window.location.href = removeSearchFromUrlParams();
    }

    // Create and populate violations
    const violationList = document.getElementById('violations');
    const createViolations = () => {
        const createViolationData = () => {
            Array.from(axeAssessedPages).forEach(page => {
                const testEngineVersion = page.testEngine.version;
                Array.from(page.violations).forEach(violation => {
                    const violationData = {
                        id: violation.id,
                        impact: violation.impact,
                        version: testEngineVersion,
                        help: violation.help,
                        helpUrl: violation.helpUrl,
                        html: Array.from(violation.nodes).map(node => node.html),
                        affects: [page.url],
                        testEngineVersion
                    };

                    const dataHash = MD5.generate(JSON.stringify(violationData));
                    const hostUrl = removeSearchFromUrlParams();
                    const permaLink = hostUrl + '?search=' + dataHash;
                    violationData.dataHash = dataHash;
                    violationData.permaLink = permaLink;

                    const existingIssue = issues.find(issue => {
                        return issue.id === violationData.id &&
                            issue.html.join(' ') === violationData.html.join(' ');
                    });

                    if (existingIssue) {
                        if (!existingIssue.affects.includes(page.url))
                            existingIssue.affects.push(page.url);
                    } else {
                        issues.push(violationData);
                    }
                });
            });
        };

        const createAndPopulateViolationElements = () => {
            issues.forEach(issue => {
                let temp = document.getElementsByTagName("template")[0];
                let clonedTemplate = temp.content.cloneNode(true);
                const li = clonedTemplate.querySelector('li');

                li.setAttribute('data-hash', issue.dataHash);
                li.setAttribute('data-impact', issue.impact);

                const helpUrl = clonedTemplate.getElementById('helpUrl');
                helpUrl.setAttribute('href', issue.helpUrl);
                helpUrl.innerText = issue.id;

                const impactTag = clonedTemplate.getElementById('impactTag');
                impactTag.setAttribute('data-tag', issue.impact);
                impactTag.innerText = issue.impact;

                const testEngine = clonedTemplate.getElementById('testEngineVersion');
                testEngine.innerText = issue.testEngineVersion;

                const violationHelp = clonedTemplate.getElementById('violationHelp');
                violationHelp.innerText = issue.help;

                const htmlSnippet = clonedTemplate.getElementById('htmlSnippet');
                htmlSnippet.innerText = issue.html;

                const urlViolationSummary = clonedTemplate.getElementById('urlViolationSummary');
                const affectsUrls = issue.affects;
                urlViolationSummary.innerText = `${affectsUrls.length} URLs`;
                const urlViolations = clonedTemplate.getElementById('urlViolations');
                affectsUrls.forEach(url => {
                    const urlListItem = document.createElement('li');
                    const urlHref = document.createElement('a');
                    urlHref.innerText = url;
                    urlListItem.appendChild(urlHref);
                    urlViolations.append(urlListItem);
                });

                const violationPermaLink = clonedTemplate.getElementById('violationPermaLink');
                violationPermaLink.setAttribute('href', issue.permaLink);
                violationPermaLink.innerText = issue.permaLink;

                violationList.appendChild(clonedTemplate);
            });
        }

        const sortByImpact = (array) => {
            const severityMap = {
                "critical": 0,
                "serious": 1,
                "moderate": 2,
                "info": 3,
            };

            array.sort((a, b) => {
                const aSeverity = severityMap[a.impact];
                const bSeverity = severityMap[b.impact];

                if (aSeverity !== bSeverity) {
                    return aSeverity - bSeverity;
                }

                return a.impact - b.impact;
            });

            return array;
        }

        createViolationData();
        sortByImpact(issues);
        createAndPopulateViolationElements();
    }
    createViolations();

    // Issue count
    const issueCount = document.getElementById('issueCount');
    const displayIssueCount = () => {
        const displayedViolations = document.querySelectorAll(`li[data-hash]`);

        const count = Array.from(displayedViolations)
            .map(liViolation => liViolation.classList.contains('hidden') ? 0 : 1)
            .reduce((accumulator, currentValue) => {
                return accumulator + currentValue
            }, 0);

        if (count === 0) {
            issueCount.innerText = "No issues identified.";
        } else {
            issueCount.innerText = `Displaying ${count} of ${issues.length} issues identified.`;
        }
    }

    // Form
    const form = document.getElementById("form");
    form.addEventListener('keypress', function (e) {
        if (e.code === 'Enter') {
            e.preventDefault();
        }
    });
    form.addEventListener("submit", function (event) {
        event.preventDefault();
    });

    // Search
    const highlighter = new Mark(document.getElementById("violations"));
    const searchViolations = (value, impactFilters) => {
        // Function to hide all violations
        const hideAllViolations = () => {
            issues.forEach(issue => {
                const dataHashFound = document.querySelector(`li[data-hash="${issue.dataHash}"]`);
                if (dataHashFound) {
                    dataHashFound.classList.add('hidden');
                }
            });
        };

        const shouldShowElement = (elementImpact, impactFilters) => {
            if (impactFilters && impactFilters.length > 0) {
                const impacts = impactFilters.map(filter => filter.impact);
                if (impacts && impacts.length > 0) {
                    return impacts.includes(elementImpact);
                }
            }
            // If no filters are applied, show the element
            return false;
        };

        // Clear any previous highlighting
        highlighter.unmark();

        // If the search input is empty, show all violations and exit
        if (value.trim() === "") {
            applyFilters();
            return;
        }

        // Hide all violations initially
        hideAllViolations();

        // Perform highlighting and filtering based on the search value
        highlighter.mark(value, {
            element: "span",
            className: "highlight",
            accuracy: "partially",
            acrossElements: true,
            each: (e) => {
                const dataHashFound = e.closest("li[data-hash]");
                if (dataHashFound) {
                    const elemDataImpact = dataHashFound.getAttribute('data-impact');
                    if (impactFilters && impactFilters.length > 0) {
                        if(shouldShowElement(elemDataImpact, impactFilters)) {
                            dataHashFound.classList.remove('hidden');
                        } else {
                            dataHashFound.classList.add('hidden');
                        }
                    } else {
                        setTimeout(() => {
                            console.log("NO!!!!", dataHashFound, "filters", impactFilters)
                            dataHashFound.classList.remove('hidden');
                        }, 250);
                    }
                }
            },
            exclude: [
                "dt"
            ],
            noMatch: () => {
                hideAllViolations();
            }
        });

        displayIssueCount();
    };

    const filterByViolationImpact = (violations) => {
        issues.forEach(issue => {
            const dataHashFound = document.querySelectorAll(`li[data-hash="${issue.dataHash}"]`);
            if (dataHashFound) {
                for (let i = 0; i < dataHashFound.length; ++i) {
                    if (violations && violations.length >= 0) {
                        dataHashFound[i].classList.add('hidden');
                    } else {
                        dataHashFound[i].classList.remove('hidden');
                    }
                }
            }
        });

        violations.forEach(violation => {
            const dataHashFound = document.querySelectorAll(`li[data-hash="${violation.dataHash}"]`);
            if (dataHashFound) {
                for (let i = 0; i < dataHashFound.length; ++i) {
                    dataHashFound[i].classList.remove('hidden');
                }
            }
        });
        displayIssueCount();
    }

    const showAllViolations = () => {
        issues.forEach(issue => {
            const dataHashFound = document.querySelectorAll(`li[data-hash="${issue.dataHash}"]`);
            if (dataHashFound) {
                for (let i = 0; i < dataHashFound.length; ++i) {
                    dataHashFound[i].classList.remove('hidden');
                }
            }
        });
        displayIssueCount();
    }

    // URL query parameters
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
            if (foundIssue) {
                searchViolations(foundIssue.dataHash, [foundIssue]);
            }
        }
    });

    const search = document.getElementById("search");
    search.addEventListener("keyup", (e) => {
        const onFilters = activeFilters(filters);
        if(onFilters && onFilters.length > 0) {
            applyFilters();
        } else {
            searchViolations(e.target.value);
        }
    });

    const clear = document.getElementById("clear");
    clear.addEventListener("click", () => {
        search.value = "";
        filters.forEach((filter) => (filter.checked = false));
        clearSearchFromUrl();
    });

    // Filters
    const activeFilters = (f) => Array.from(f).reduce((active, filter) => {
        return active.concat(filter.checked ? filter.value : []);
    }, []);

    const applyFilters = () => {
        let violationByImpact = [];
        const onFilters = activeFilters(filters);
        const searchValue = document.getElementById('search');
        if (onFilters.length > 0) {
            violationByImpact = issues.filter((issue) => {
                return onFilters.includes(issue.impact);
            });

            if (searchValue && searchValue.value.trim() !== "") {
                searchViolations(searchValue.value, violationByImpact);
            } else {
                filterByViolationImpact(violationByImpact);
            }
        } else {
            if (searchValue && searchValue.value.trim() !== "") {
                searchViolations(searchValue.value);
            } else {
                showAllViolations();
            }
        }
    }

    // Impact toggles
    const filters = document.querySelectorAll("input[name='impact']");
    filters.forEach((filter) => {
        filter.addEventListener("input", applyFilters);
    });

    displayIssueCount();

    // Visibility (based on JavaScript enabled/disabled in browser)
    document.getElementById("sidebar").classList.remove("js-hidden");
}