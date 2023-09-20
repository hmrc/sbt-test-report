/**
 * @jest-environment jsdom
 */

const {test, describe, beforeEach, expect} = require('@jest/globals');
const metaDataHeader = require("../../src/main/resources/assets/js/metaDataHeader");
let reportMetaDataElement;

beforeEach(() => {
    document.body.innerHTML =
        '<div>' +
            '<div id="metaDataHeader"></div>' +
        '</div>';

    reportMetaDataElement = document.getElementById('metaDataHeader');
})

describe('with Jenkins build metadata', () => {
    test('includes link to Jenkins build', () => {
        const reportMetaData = {
            projectName: "platform-example-ui-tests",
            jenkinsBuildId: "#42",
            jenkinsBuildUrl: "https://build.tax.service.gov.uk/job/Platform-Testing/job/Examples/job/platform-example-ui-tests/42/",
            dateOfAssessment: "09-11-2023"
        }
        reportMetaData.testEnvironment = "Chrome"; // TODO: should be apart of the report meta data json

        metaDataHeader(reportMetaDataElement, reportMetaData);

        const metaData = document.querySelector('p');
        expect(metaData.textContent).toBe("Generated from build #42 (Chrome) of platform-example-ui-tests on 09-11-2023");

        const linkToBuild = document.querySelector('a:nth-child(1)');
        expect(linkToBuild.textContent).toBe('#42');
        expect(linkToBuild.href).toBe('https://build.tax.service.gov.uk/job/Platform-Testing/job/Examples/job/platform-example-ui-tests/42/');

        const linkToProject = document.querySelector('a:nth-child(2)');
        expect(linkToProject.textContent).toBe('platform-example-ui-tests');
        expect(linkToProject.href).toBe('https://github.com/hmrc/platform-example-ui-tests');

        const timeElement = document.querySelector('time');
        expect(timeElement.dateTime).toBe('09-11-2023');
    });
});

describe("without Jenkins build metadata (eg. run locally)", () => {
    test('include link to project and run date', () => {
        const reportMetaData = {
            projectName: "platform-example-ui-tests",
            jenkinsBuildId: null,
            jenkinsBuildUrl: null,
            dateOfAssessment: "09-12-2023"
        }
        reportMetaData.testEnvironment = "Chrome"; // TODO: should be apart of the report meta data json

        metaDataHeader(reportMetaDataElement, reportMetaData);

        const metaData = document.querySelector('p');
        expect(metaData.textContent).toBe("Generated from platform-example-ui-tests on 09-12-2023");

        const linkToProject = document.querySelector('a');
        expect(linkToProject.textContent).toBe('platform-example-ui-tests');
        expect(linkToProject.href).toBe('https://github.com/hmrc/platform-example-ui-tests');

        const timeElement = document.querySelector('time');
        expect(timeElement.dateTime).toBe('09-12-2023');
    });
});