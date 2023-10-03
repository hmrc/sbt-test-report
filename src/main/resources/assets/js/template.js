export function populateTemplate(template, issue) {
    let clonedTemplate = template.content.cloneNode(true);

    const li = clonedTemplate.querySelector('li');
    li.setAttribute('data-hash', issue.dataHash);
    li.setAttribute('data-impact', issue.impact);

    const helpUrl = clonedTemplate.getElementById('helpUrl');
    helpUrl.setAttribute('href', issue.helpUrl);
    helpUrl.textContent = issue.id;

    const impactTag = clonedTemplate.getElementById('impactTag');
    impactTag.setAttribute('data-tag', issue.impact);
    impactTag.textContent = issue.impact;

    const testEngine = clonedTemplate.getElementById('testEngineVersion');
    testEngine.textContent = issue.testEngineVersion;

    const violationHelp = clonedTemplate.getElementById('violationHelp');
    violationHelp.textContent = issue.help;

    const htmlSnippet = clonedTemplate.getElementById('htmlSnippet');
    htmlSnippet.textContent = issue.html;

    const urlViolationSummary = clonedTemplate.getElementById('urlViolationSummary');
    const affectsUrls = issue.affects;
    urlViolationSummary.textContent = `${affectsUrls.length} URLs`;

    const urlViolations = clonedTemplate.getElementById('urlViolations');
    affectsUrls.forEach(url => {
        const urlListItem = document.createElement('li');
        urlListItem.textContent = url;
        urlViolations.append(urlListItem);
    });

    const violationPermaLink = clonedTemplate.getElementById('violationPermaLink');
    violationPermaLink.setAttribute('href', issue.permaLink);
    violationPermaLink.textContent = issue.permaLink;

    return clonedTemplate;
}