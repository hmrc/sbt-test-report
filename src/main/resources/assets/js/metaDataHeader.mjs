function createTimestampSection(dateOfAssessment) {
    const timeStamp = document.createElement('time');
    timeStamp.dateTime = dateOfAssessment;
    timeStamp.textContent = dateOfAssessment;

    return timeStamp;
}

function createProjectNameSection(projectName) {
    const arefProjectName = document.createElement('a');
    arefProjectName.href = "https://github.com/hmrc/" + projectName;
    arefProjectName.textContent = projectName;
    arefProjectName.target = "_blank";

    return arefProjectName;
}

function addBuildSection(paragraph, jenkinsBuildId, jenkinsBuildUrl, testEnvironment) {
    if (jenkinsBuildId) {
        paragraph.innerHTML += "build ";

        const arefBuildUrl = document.createElement('a');
        arefBuildUrl.href = jenkinsBuildUrl ? jenkinsBuildUrl : '#';
        arefBuildUrl.textContent = jenkinsBuildId;

        const spanBuildNumber = document.createElement('span');
        spanBuildNumber.appendChild(arefBuildUrl);
        paragraph.appendChild(spanBuildNumber);

        let userAgent = "Chrome"; // only chrome and edge are supported so default to chrome if user agent is not edge
        if (testEnvironment && testEnvironment.includes('Edg/')) {
            userAgent = "Edge"
        }
        paragraph.innerHTML += ' (' + userAgent + ')' + ' of '
    }

    return paragraph;
}

function metaDataHeader(parentElement, reportMetaData) {
    if (reportMetaData) {
        const paragraph = document.createElement('p');
        paragraph.innerHTML = "Generated from ";

        addBuildSection(paragraph, reportMetaData.jenkinsBuildId, reportMetaData.jenkinsBuildUrl, reportMetaData.testEnvironment);

        paragraph.appendChild(createProjectNameSection(reportMetaData.projectName));

        paragraph.innerHTML += ' on ';

        paragraph.appendChild(createTimestampSection(reportMetaData.dateOfAssessment));

        parentElement.appendChild(paragraph);
    }
}

module.exports = metaDataHeader; // TODO: need webpack to package assets for ES6