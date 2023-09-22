function createGroupedIssues(pages, funcMD5, funcRemoveSearchFromUrlParams) {
    const issues = [];

    Array.from(pages).forEach(page => {
        const testEngineVersion = page.testEngine.version;
        Array.from(page.violations).forEach(violation => {
            const violationData = {
                id: violation.id,
                impact: violation.impact,
                help: violation.help,
                helpUrl: violation.helpUrl,
                html: Array.from(violation.nodes).map(node => node.html),
                affects: [page.url],
                testEngineVersion
            };

            const dataHash = funcMD5.generate(JSON.stringify(violationData));
            const hostUrl = funcRemoveSearchFromUrlParams();
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

    return issues;
}

function sortByImpact(array) {
    const severityMap = {
        "critical": 0,
        "serious": 1,
        "moderate": 2,
        "info": 3,
    };

    array.sort((a, b) => {
        const aSeverity = severityMap[a.impact];
        const bSeverity = severityMap[b.impact];
        return aSeverity - bSeverity;
    });

    return array;
}

module.exports = {createGroupedIssues, sortByImpact};
