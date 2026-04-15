<%@ page errorPage="../../ErrorPage.jsp" %>
<jsp:include page="../../AdminHeader.jsp" />

<%@page import="fr.paris.lutece.plugins.solrserver.web.SolrserverJspBean"%>
 
${ solrserverJspBean.init( pageContext.request, SolrserverJspBean.RIGHT_MANAGE_SOLRSERVER ) }
${ solrserverJspBean.getForm( pageContext.request ) }


<%@ include file="../../AdminFooter.jsp" %>