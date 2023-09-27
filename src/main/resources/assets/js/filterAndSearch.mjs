import {clearSearchFromUrl, debounce} from "./browserHelper.mjs";

export function initialiseFilterAndSearch(violationList, groupedIssues) {
    // Visibility (based on JavaScript enabled/disabled in browser)
    document.getElementById("sidebar").classList.remove("js-hidden");

    // Prevent default form behaviours
    const form = document.getElementById("form");
    form.addEventListener('keypress', function (e) {
        if (e.code === 'Enter') {
            e.preventDefault();
        }
    });
    form.addEventListener("submit", function (event) {
        event.preventDefault();
    });

    // Issue count
    const updateVisibleIssuesCount = () => {
        const issueCount = document.getElementById('issueCount');

        const allIssues = Array.from(document.querySelectorAll(`li[data-impact]`));

        const visibleIssuesCount = allIssues
            .filter(liViolation => !liViolation.classList.contains('hidden'))
            .length;

        if (visibleIssuesCount === 0) {
            issueCount.textContent = "No issues identified.";
        } else {
            issueCount.textContent = `Displaying ${visibleIssuesCount} of ${allIssues.length} issues identified.`;
        }
    }

    // Search
    const search = document.getElementById("search");
    const highlighter = new Mark(violationList);

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
            updateVisibleIssuesCount();
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
            each: (e) => { // TODO named function?
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
                            updateVisibleIssuesCount();
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

        updateVisibleIssuesCount();
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
        updateVisibleIssuesCount();
    }

    const filters = document.querySelectorAll("input[name='impact']");

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

        const onFilters = activeFilters(filters); // TODO: add active filters to url parameter
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
        const listIssues = document.querySelectorAll('li[data-hash]');
        if (listIssues) {
            Array.from(listIssues).forEach(elem => {
                const elemDataImpact = elem.getAttribute('data-impact');
                if (onFilters.includes(elemDataImpact)) {
                    elem.classList.remove('hidden');
                } else {
                    elem.classList.add('hidden');
                }
            })
        }

        updateVisibleIssuesCount();
    }

    filters.forEach((filter) => {
        filter.addEventListener("input", applyFilters);
    });

    // Clear search and filters
    const clear = document.getElementById("clear");
    clear.addEventListener("click", () => {
        search.value = "";
        filters.forEach((filter) => (filter.checked = false));
        clearSearchFromUrl();
    });

    // URL query parameters
    const initialSearchParams = new URLSearchParams(window.location.search);
    initialSearchParams.forEach((valueFromUrl, name) => {
        if (name === "search") {
            search.value = valueFromUrl;
            searchViolations(activeFilters(filters)); // TODO: active filters are not stored on the url as parameters so they will not be included
        }
    });

    // Update count on page load
    updateVisibleIssuesCount();

}