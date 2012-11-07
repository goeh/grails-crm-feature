<%@ page contentType="text/html;charset=UTF-8" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta name="layout" content="main"/>
    <title><g:message code="crmFeature.index.title" default="Feature Management"/></title>
    <style type="text/css">
        tr.disabled td {
            color: #999;
        }
        tr.disabled td.status {
            color: #990000;
        }
        tr.enabled td.status {
            color: #009900;
        }
    </style>
</head>

<body>

<header class="page-header">
    <h1><g:message code="crmFeature.index.title" default="Feature Management"/></h1>
</header>

<div class="row-fluid">
    <div class="span6">

        <h3>Application features</h3>
        <table class="table table-striped">
            <thead>
            <tr>
                <th>Feature</th>
                <th>Description</th>
                <th>Enabled</th>
                <th>Expires</th>
            </tr>
            </thead>
            <tbody>
            <g:each in="${applicationFeatures}" var="feature">
                <tr class="${feature.enabled ? 'enabled' : 'disabled'}">
                    <td>${feature.name.encodeAsHTML()}</td>
                    <td>${feature.description?.encodeAsHTML()}</td>
                    <td class="status">${feature.enabled ? 'YES' : 'NO'}</td>
                    <td style="white-space: nowrap;"><g:formatDate format="yyyy-MM-dd HH:mm" date="${feature.expires}"/></td>
                </tr>
            </g:each>
            </tbody>
        </table>

    </div>

    <div class="span6">
        <g:if test="${tenant}">
            <h3>${tenant.name.encodeAsHTML()} features
                <small>${tenant.user?.username ?: 'no account'}</small>
            </h3>
            <table class="table table-striped">
                <thead>
                <tr>
                    <th>Feature</th>
                    <th>Description</th>
                    <th>Enabled</th>
                    <th>Expires</th>
                </tr>
                </thead>
                <tbody>
                <g:each in="${tenantFeatures}" var="feature">
                    <tr class="${feature.enabled ? 'enabled' : 'disabled'}">
                        <td>${feature.name.encodeAsHTML()}</td>
                        <td>${feature.description?.encodeAsHTML()}</td>
                        <td class="status">${feature.enabled ? 'YES' : 'NO'}</td>
                        <td style="white-space: nowrap;"><g:formatDate date="${feature.expires}"/></td>
                    </tr>
                </g:each>
                </tbody>
            </table>
        </g:if>
    </div>
</div>

</body>
</html>
