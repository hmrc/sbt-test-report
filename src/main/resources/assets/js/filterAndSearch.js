import {debounce, updateUrlParam} from "./browserHelper.mjs";

export function initialiseFilterAndSearch(violationList) {
    // model for page state
    const pageState = {
        searchTerm: "",
        searchMatches: [],
        activeFilters: []
    }

    const url = new URL(window.location.href);
    const highlighter = new Mark(violationList);
    const filters = document.querySelectorAll("input[name='impact']");
    const searchInput = document.getElementById("search");
    const issueCount = document.getElementById('issueCount');

    // Visibility (based on JavaScript enabled/disabled in browser)
    document.getElementById("sidebar").classList.remove("js-hidden");

    setupFilters();
    const { findMatches } = setupSearch();
    maybeApplyUrlParametersOnPageLoad();
    preventDefaultFormBehaviours();

    function applyPageState() {
        const isVisible = (pageState, dataHash, dataImpact) => {
            return (pageState.searchMatches.includes(dataHash) || pageState.searchMatches.length === 0) &&
                (pageState.activeFilters.includes(dataImpact) || pageState.activeFilters.length === 0);
        }

        // TODO
        const expandAffectsLinks = (dataHash) => {
            const elemListItem = document.querySelector(`li[data-hash="${dataHash}"]`);
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

        // show/hide issues based on page state
        const listIssues = document.querySelectorAll('li[data-hash]');

        const visibleIssuesCount = Array.from(listIssues).map(elem => {
            const elemDataHash = elem.getAttribute('data-hash');
            const elemDataImpact = elem.getAttribute('data-impact');

            if (pageState.searchTerm && pageState.searchMatches.length === 0) {
                elem.classList.add('hidden');
                return 0;
            } else {
                if (isVisible(pageState, elemDataHash, elemDataImpact)) {
                    elem.classList.remove('hidden');
                    // expandAffectsLinks(elemDataHash); // TODO where should this be
                    return 1;
                } else {
                    elem.classList.add('hidden');
                    return 0;
                }
            }
        }).reduce((acc, num) => acc + num, 0);

        // update visible issues count based on page state
        if (visibleIssuesCount === 0) {
            issueCount.textContent = "No issues identified.";
        } else {
            issueCount.textContent = `Displaying ${visibleIssuesCount} of ${listIssues.length} issues identified.`;
        }

        // update query params
        updateUrlParam(url, 'search', pageState.searchTerm);
        updateUrlParam(url, 'filters', pageState.activeFilters.join(','));

        //     update filter controls
        filters.forEach((filter) => (filter.checked = false));
        pageState.activeFilters.forEach((filter) => {
            const filterElem = document.getElementById(`impact-${filter}`);
            if (filterElem) {
                filterElem.checked = true;
            }
        });

        // populate search box
        searchInput.value = pageState.searchTerm;
    }

    function setupFilters() {
        const activeFilters = (f) => Array.from(f).reduce((active, filter) => {
            return active.concat(filter.checked ? filter.value : []);
        }, []);

        const applyFilters = () => {
            pageState.activeFilters = activeFilters(filters);
            applyPageState();
        }

        filters.forEach((filter) => {
            filter.addEventListener("input", applyFilters);
        });
    }

    function setupSearch() {
        const onSearchTermMatch = (element) => {
            const dataHashFound = element.closest("li[data-hash]");
            if (dataHashFound) {
                const elemDataHash = dataHashFound.getAttribute('data-hash');
                pageState.searchMatches.push(elemDataHash);
            }
        }

        const onSearchTermNoMatch = () => {
            pageState.searchMatches = [];
        }

        const findMatches = () => {
            // Clear any previous highlighting
            highlighter.unmark();

            // If the search term is empty, show all violations and return
            if (pageState.searchTerm === "") {
                pageState.searchMatches = [];
                return;
            }

            // Perform highlighting and filtering based on the search term
            highlighter.mark(pageState.searchTerm, {
                element: "span",
                exclude: ["dt"],
                acrossElements: true,
                accuracy: "partially",
                className: "highlight",
                each: onSearchTermMatch,
                noMatch: onSearchTermNoMatch
            });
        }

        const searchViolations = () => {
            findMatches();
            applyPageState();
        };

        const debounceSearch = debounce(searchViolations, 500);
        searchInput.addEventListener("keyup", (e) => {
            pageState.searchTerm = e.target.value.trim();
            debounceSearch();
        });

        return { findMatches }
    }

    function preventDefaultFormBehaviours() {
        const form = document.getElementById("form");
        form.addEventListener('keypress', function (e) {
            if (e.code === 'Enter') {
                e.preventDefault();
            }
        });
        form.addEventListener("submit", function (event) {
            event.preventDefault();
        });
    }

    // Apply URL query parameters on page load
    function maybeApplyUrlParametersOnPageLoad() {
        url.searchParams.forEach((valueFromUrl, name) => {
            if (name === "search") {
                pageState.searchTerm = valueFromUrl;
                findMatches();
            } else if (name === 'filters') {
                if (valueFromUrl) {
                    pageState.activeFilters = valueFromUrl.split(',');
                }
            }
        });
        applyPageState();
    }

    const clear = document.getElementById("clear");
    clear.addEventListener("click", () => {
        // Clear any previous highlighting
        highlighter.unmark();

        pageState.searchTerm = '';
        pageState.searchMatches = [];
        pageState.activeFilters = [];

        applyPageState();
    });
}