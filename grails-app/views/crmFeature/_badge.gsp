<%@ page contentType="text/html;charset=UTF-8" defaultCodec="html" %>
<%
    def badge
    switch (usage) {
        case 'none':
            badge = 'badge-important'
            break
        case 'low':
            badge = 'badge-warning'
            break
        case 'medium':
            badge = 'badge-info'
            break
        case 'high':
            badge = 'badge-success'
            break
        default:
            badge = ''
            break
    }
%>

<g:if test="${objects != null}">
    <span class="badge ${badge}">${objects}</span>
</g:if>
