function metaDataHeader(reportMetaData) {
    if (reportMetaData) {
        const reportMetaDataElement = document.getElementById('metaDataHeader');
        const spanBuildNumber = document.createElement('span');
        const paragraph = document.createElement('p');
        if (reportMetaData.jenkinsBuildId) {
            paragraph.innerHTML = "Generated from build ";
            const arefBuildUrl = document.createElement('a');
            arefBuildUrl.href = reportMetaData.jenkinsBuildUrl;
            arefBuildUrl.textContent = reportMetaData.jenkinsBuildId;
            spanBuildNumber.appendChild(arefBuildUrl);
            paragraph.appendChild(spanBuildNumber);

            let userAgent = "Chrome";
            if (reportMetaData.testEnvironment && reportMetaData.testEnvironment.includes('Edg/')) {
                userAgent = "Edge"
            }
            paragraph.innerHTML += ' (' + userAgent + ')' + ' of '
        } else {
            paragraph.innerHTML = "Generated from ";
        }

        const arefProjectName = document.createElement('a');
        arefProjectName.href = "https://github.com/hmrc/" + reportMetaData.projectName;
        arefProjectName.textContent = reportMetaData.projectName;
        arefProjectName.target = "_blank";
        paragraph.appendChild(arefProjectName);
        paragraph.innerHTML += ' on ';
        const timeStamp = document.createElement('time');
        timeStamp.dateTime = reportMetaData.dateOfAssessment;
        timeStamp.textContent = reportMetaData.dateOfAssessment;
        paragraph.appendChild(timeStamp);
        reportMetaDataElement.appendChild(paragraph);

        const footerProjectLinkElement = document.getElementById('footerProjectLink');
        footerProjectLinkElement.append(arefProjectName);
    }
}

module.exports = metaDataHeader;