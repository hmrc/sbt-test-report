import {populateTemplate} from "./template.mjs";
import {metaDataHeader} from "./metaDataHeader.mjs";
import {createGroupedIssues, sortByImpact} from "./issues.mjs";
import {initialiseFilterAndSearch} from "./filterAndSearch.mjs";

export function init() {
    const {reportMetaData, axeAssessedPages} = reportData();
    const violationList = document.getElementById('violations');
    const template = document.getElementsByTagName("template")[0];
    const reportMetaDataElement = document.getElementById('metaDataHeader');

    // Create page header with report metadata
    reportMetaData.testEnvironment =
        axeAssessedPages && axeAssessedPages.length > 0 &&
        axeAssessedPages[0].testEnvironment.userAgent; // TODO: should be a part of the report meta data json
    metaDataHeader(reportMetaDataElement, reportMetaData);

    // Create and populate issues
    const groupedIssues = createGroupedIssues(axeAssessedPages);
    sortByImpact(groupedIssues).forEach(issue => {
        const clonedTemplate = populateTemplate(template, issue)
        violationList.appendChild(clonedTemplate);
    });

    initialiseFilterAndSearch(violationList, groupedIssues);
}

document.body.addEventListener("load", init(), false);