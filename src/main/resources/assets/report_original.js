async function init() {
    /*const issues = new List("issues", {
        valueNames: [
            "id",
            "impact",
            "version",
            "help",
            "html",
            "affects",
            "permalink",
            {
                data: ["hash"],
            },
        ]
    })

    document.issues = issues;*/

    // Read axe json results
    const axeAssessedPages = ['%INJECT_AXE_VIOLATIONS%'];

    const violationList = document.getElementById('violations');
    Array.from(axeAssessedPages).forEach(page => {
        const testEngineVersion = page.testEngine.version;
        Array.from(page.violations).forEach(violation => {
            let temp = document.getElementsByTagName("template")[0];
            let clonedTemplate = temp.content.cloneNode(true);
            const li = clonedTemplate.querySelector('li');
            const dataHash = crypto.randomUUID();
            li.setAttribute('data-hash', dataHash);
            li.setAttribute('data-impact', violation.impact);
            const helpUrl = clonedTemplate.getElementById('helpUrl');
            helpUrl.setAttribute('href', violation.helpUrl);
            helpUrl.innerText = violation.id;
            const impactTag = clonedTemplate.getElementById('impactTag');
            impactTag.setAttribute('data-tag', violation.impact);
            impactTag.innerText = violation.impact;
            //testEngineVersion
            const testEngine = clonedTemplate.getElementById('testEngineVersion');
            testEngine.innerText = testEngineVersion;
            //violationHelp
            const violationHelp = clonedTemplate.getElementById('violationHelp');
            violationHelp.innerText = violation.help;
            // htmlSnippet
            const htmlSnippet = clonedTemplate.getElementById('htmlSnippet');
            htmlSnippet.innerText = Array.from(violation.nodes).map(node => node.html);
            // urlViolations
            const urlViolation = page.url;
            const urlViolationSummary = clonedTemplate.getElementById('urlViolationSummary');
            urlViolationSummary.innerText = '1 URLs';
            const urlViolations = clonedTemplate.getElementById('urlViolations');
            const urlListItem = document.createElement('li');
            const urlHref = document.createElement('a');
            urlHref.innerText = urlViolation;
            urlListItem.appendChild(urlHref);
            urlViolations.append(urlListItem);
            const violationPermaLink = clonedTemplate.getElementById('violationPermaLink');
            const permaLink = window.location.href + '?search=' + dataHash;
            violationPermaLink.setAttribute('href', permaLink);
            violationPermaLink.innerText = permaLink;

            violationList.appendChild(clonedTemplate);
        });
    });

    // Visibility (based on JavaScript enabled/disabled in browser)
    document.getElementById("sidebar").classList.remove("js-hidden");

    // Form
    const form = document.getElementById("form");
    form.addEventListener("submit", function (event) {
        event.preventDefault();
    });

    // Clear
    const clear = document.getElementById("clear");

    clear.addEventListener("click", () => {
        search.value = "";
        filters.forEach((filter) => (filter.checked = false));

        issues.search();
        issues.filter();
    });

    // Dynamic updates
    issues.on("updated", function () {
        displayIssueCount();
        updateUrl();
    });

    // Search
    const search = document.getElementById("search");

    // Init
    let initialSearchParams = new URLSearchParams(window.location.search);

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
            issues.search(valueFromUrl);
        }
    });

    // URL query parameters
    function updateUrl() {
        const url = new URL(window.location);
        url.search = new URLSearchParams(new FormData(form));
        window.history.pushState({}, "", url);
    }

    const issueCount = document.getElementById('issueCount');

    function displayIssueCount() {
        if (issues.items.length === 0) {
            issueCount.innerText = "No issues identified.";
        } else {
            issueCount.innerText = `Displaying ${issues.visibleItems.length} of ${issues.items.length} issues identified.`;
        }
    }

    function applyFilters() {
        const activeFilters = Array.from(filters).reduce((active, filter) => {
            return active.concat(filter.checked ? filter.value : []);
        }, []);

        if (activeFilters.length) {
            issues.filter((issue) => {
                return activeFilters.includes(issue.values().impact);
            });
        } else {
            issues.filter();
        }
    }

    displayIssueCount();

    // Filters
    const filters = document.querySelectorAll("input[name='impact']");

    filters.forEach((filter) => {
        filter.addEventListener("input", applyFilters);
    });

    // Remove "?search=" from start URL when search not applied
    if (window.location.search !== "") {
        applyFilters();
    }
}

