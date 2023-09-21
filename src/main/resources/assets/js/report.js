async function init() {
    let groupedIssues = [];
    const {reportMetaData, axeAssessedPages} = reportData();

    // Init
    let initialSearchParams = new URLSearchParams(window.location.search);

    const search = document.getElementById("search");
    const violationList = document.getElementById('violations');
    const reportMetaDataElement = document.getElementById('metaDataHeader');

    // Create page header with report metadata
    reportMetaData.testEnvironment = axeAssessedPages && axeAssessedPages.length > 0 && axeAssessedPages[0].testEnvironment.userAgent; // TODO: should be apart of the report meta data json
    metaDataHeader(reportMetaDataElement, reportMetaData);

    const debounce = (func, delay) => {
        let timeoutId;

        return function () {
            const context = this;
            const args = arguments;

            clearTimeout(timeoutId);

            timeoutId = setTimeout(function () {
                func.apply(context, args);
            }, delay);
        };
    }

    // Create and populate violations
    const createIssues = () => {

        const populateIssues = (groupedIssues) => {
            groupedIssues.forEach(issue => {
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
                    urlListItem.innerText = url;
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

        groupedIssues = createGroupedIssues(axeAssessedPages, MD5, removeSearchFromUrlParams);
        sortByImpact(groupedIssues);
        populateIssues(groupedIssues);
    }
    createIssues();

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
            issueCount.innerText = `Displaying ${count} of ${groupedIssues.length} issues identified.`;
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

    const minimumSearchLength = 1;
    const searchTermNotValid = (term) => {
        return term.trim().length < minimumSearchLength;
    }

    const searchTermValid = (term) => {
        return term.trim().length >= minimumSearchLength;
    }

    // Search
    const highlighter = new Mark(document.getElementById("violations"));
    const searchViolations = (onFilters) => {
        const value = search.value;

        // Function to hide all violations
        const hideAllViolations = () => {
            groupedIssues.forEach(issue => {
                const dataHashFound = document.querySelector(`li[data-hash="${issue.dataHash}"]`);
                if (dataHashFound) {
                    dataHashFound.classList.add('hidden');
                }
            });
        };

        // Clear any previous highlighting
        highlighter.unmark();
        if(searchTermNotValid(value)) return;

        // If the search input is empty, show all violations and exit
        if (value.trim() === "") {
            applyFilters();
            displayIssueCount();
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
                    if (onFilters && onFilters.length > 0) {
                        if (onFilters.includes(elemDataImpact)) {
                            dataHashFound.classList.remove('hidden');
                        }
                    } else {
                        setTimeout(() => {
                            dataHashFound.classList.remove('hidden');
                            displayIssueCount();
                        }, 250);
                    }

                    // check for affects links and expand
                    const elemDataHash = dataHashFound.getAttribute('data-hash');
                    const elemListItem = document.querySelector(`li[data-hash="${elemDataHash}"]`);
                    if (elemListItem) {
                        const affectsLink = elemListItem.querySelector('#urlViolations');
                        if (affectsLink) {
                            const affectsDetails = affectsLink.closest('details');
                            if (affectsDetails) {
                                affectsDetails.setAttribute('open', '');
                            }
                        }
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

    const filterByViolationImpact = (onFilters) => {
        groupedIssues.forEach(issue => {
            const dataHashFound = document.querySelectorAll(`li[data-hash="${issue.dataHash}"]`);
            if (dataHashFound) {
                Array.from(dataHashFound).forEach(elem => {
                    const elemDataImpact = elem.getAttribute('data-impact');
                    if (onFilters.includes(elemDataImpact)) {
                        elem.classList.remove('hidden');
                    } else {
                        elem.classList.add('hidden');
                    }
                })
            }
        });
        displayIssueCount();
    }

    const showAllViolations = () => {
        groupedIssues.forEach(issue => {
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
            const foundIssue = groupedIssues.find(issue => issue.dataHash === valueFromUrl);
            if (foundIssue) {
                const violationElem = document.querySelector(`li[data-hash="${foundIssue.dataHash}"]`);
                if (violationElem) {
                    const elemDataImpact = violationElem.getAttribute('data-impact');
                    search.value = foundIssue.dataHash;
                    searchViolations([elemDataImpact]);
                }
            }
        }
    });

    const debounceSearch = debounce(searchViolations, 500);
    search.addEventListener("keyup", (e) => {
        // Clear any previous highlighting
        highlighter.unmark();

        const onFilters = activeFilters(filters);
        if (onFilters && onFilters.length > 0) {
            applyFilters();
        } else {
            debounceSearch();
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
        // Clear any previous highlighting
        highlighter.unmark();

        const onFilters = activeFilters(filters);
        const searchValue = document.getElementById('search');
        if (onFilters.length > 0) {
            if (searchValue && searchTermValid(searchValue.value) && searchValue.value.trim() !== "") {
                searchViolations(onFilters);
            } else {
                filterByViolationImpact(onFilters);
            }
        } else {
            if (searchValue && searchTermValid(searchValue.value) && searchValue.value.trim() !== "") {
                searchViolations();
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