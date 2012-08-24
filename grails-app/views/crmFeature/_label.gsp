<%@ page contentType="text/html;charset=UTF-8" defaultCodec="html" %>
<%
    def label
    switch (usage) {
        case 'none':
            label = 'label-important'
            break
        case 'low':
            label = 'label-warning'
            break
        case 'medium':
            label = 'label-info'
            break
        case 'high':
            label = 'label-success'
            break
        default:
            label = ''
            break
    }
%>

<span class="label ${label}">
<g:if test="${usage}">
    <g:message code="crmFeature.statistics.usage.label" default="Usage:"/>
    ${message(code: 'crmFeature.statistics.usage.' + usage, default: usage)}
</g:if>

<g:if test="${objects != null}">
    <g:message code="crmFeature.statistics.objects.label" default="Objects: {0}" args="${[objects]}"/>
</g:if>
</span>
