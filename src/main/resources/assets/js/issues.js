import md5 from '../../assets/lib/md5.js';

export function createGroupedIssues(pages) {
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

            const dataHash = md5(JSON.stringify(violationData));
            const permaLink = window.location.origin + '?search=' + dataHash;
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

export function sortByImpact(array) {
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
