import {populateTemplate} from "./template.mjs";
import {metaDataHeader} from "./metaDataHeader.mjs";
import {createGroupedIssues, sortByImpact} from "./issues.mjs";
import {clearSearchFromUrl, debounce} from "./browserHelper.mjs";

export function init() {
    const {reportMetaData, axeAssessedPages} = reportData();
    const form = document.getElementById("form");
    const clear = document.getElementById("clear");
    const search = document.getElementById("search");
    const initialSearchParams = new URLSearchParams(window.location.search);
    const violationList = document.getElementById('violations');
    const highlighter = new Mark(violationList);
    const issueCount = document.getElementById('issueCount');
    const filters = document.querySelectorAll("input[name='impact']");
    const template = document.getElementsByTagName("template")[0];
    const reportMetaDataElement = document.getElementById('metaDataHeader');

    // Visibility (based on JavaScript enabled/disabled in browser)
    document.getElementById("sidebar").classList.remove("js-hidden");

    // Prevent default form behaviours
    form.addEventListener('keypress', function (e) {
        if (e.code === 'Enter') {
            e.preventDefault();
        }
    });
    form.addEventListener("submit", function (event) {
        event.preventDefault();
    });

    // Create page header with report metadata
    reportMetaData.testEnvironment =
        axeAssessedPages && axeAssessedPages.length > 0 &&
        axeAssessedPages[0].testEnvironment.userAgent; // TODO: should be apart of the report meta data json
    metaDataHeader(reportMetaDataElement, reportMetaData);

    // Create and populate issues
    const groupedIssues = createGroupedIssues(axeAssessedPages);
    sortByImpact(groupedIssues).forEach(issue => {
        const clonedTemplate = populateTemplate(template, issue)
        violationList.appendChild(clonedTemplate);
    });

    // Issue count
    const displayIssueCount = () => {
        const displayedViolations = document.querySelectorAll(`li[data-hash]`);

        const count = Array.from(displayedViolations)
            .map(liViolation => liViolation.classList.contains('hidden') ? 0 : 1)
            .reduce((accumulator, currentValue) => {
                return accumulator + currentValue
            }, 0);

        if (count === 0) {
            issueCount.textContent = "No issues identified.";
        } else {
            issueCount.textContent = `Displaying ${count} of ${groupedIssues.length} issues identified.`;
        }
    }

    // Search
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
            if (searchValue && searchValue.value.trim() !== "") {
                searchViolations(onFilters);
            } else {
                filterByViolationImpact(onFilters);
            }
        } else {
            if (searchValue && searchValue.value.trim() !== "") {
                searchViolations();
            } else {
                showAllViolations();
            }
        }
    }

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

    filters.forEach((filter) => {
        filter.addEventListener("input", applyFilters);
    });

    // Clear search and filters
    clear.addEventListener("click", () => {
        search.value = "";
        filters.forEach((filter) => (filter.checked = false));
        clearSearchFromUrl();
    });

    // URL query parameters
    initialSearchParams.forEach((valueFromUrl, name) => {
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

    // Update count on page load
    displayIssueCount();
}

document.body.addEventListener("load", init(), false);