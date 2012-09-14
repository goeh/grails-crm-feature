<%@ page contentType="text/html;charset=UTF-8" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta name="layout" content="main"/>
    <g:set var="featureName" value="${message(code: 'feature.' + feature.name + '.label', default: feature.name)}"/>
    <title><g:message code="crmFeature.uninstall.title" default="Uninstall feature {0}"
                      args="${[featureName]}"/></title>
</head>

<body>

<header class="page-header">
    <h1><g:message code="crmFeature.uninstall.title" default="Uninstall feature {0}" args="${[featureName]}"/></h1>
</header>

<g:form action="uninstall">

    <g:hiddenField name="id" value="${tenant}"/>
    <g:hiddenField name="name" value="${feature.name}"/>
    <g:hiddenField name="referer" value="${params.referer}"/>

    <div class="form-actions">
        <crm:button visual="danger" icon="icon-trash icon-white" label="crmFeature.button.uninstall.label"
                    confirm="crmFeature.button.uninstall.confirm"/>
        <crm:button type="link" controller="shiroCrmTenant" action="edit" id="${tenant}" fragment="features"
                    icon="icon-remove"
                    label="crmFeature.button.cancel.label"/>
    </div>

</g:form>
</body>
</html>