<%@ page contentType="text/html;charset=UTF-8" defaultCodec="html" %>

<g:if test="${usage}">
    <g:message code="crmFeature.statistics.usage.label" default="Usage:"/>
    ${message(code: 'crmFeature.statistics.usage.' + usage, default: usage)}
</g:if>

<g:if test="${objects != null}">
    <g:message code="crmFeature.statistics.objects.label" default="Objects: {0}" args="${[objects]}"/>
</g:if>
