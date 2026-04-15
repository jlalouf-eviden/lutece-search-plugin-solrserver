/*
	 * Copyright (c) 2002-2026, City of Paris
	 * All rights reserved.
	 *
	 * Redistribution and use in source and binary forms, with or without
	 * modification, are permitted provided that the following conditions
	 * are met:
	 *
	 *  1. Redistributions of source code must retain the above copyright notice
	 *     and the following disclaimer.
	 *
	 *  2. Redistributions in binary form must reproduce the above copyright notice
	 *     and the following disclaimer in the documentation and/or other materials
	 *     provided with the distribution.
	 *
	 *  3. Neither the name of 'Mairie de Paris' nor 'Lutece' nor the names of its
	 *     contributors may be used to endorse or promote products derived from
	 *     this software without specific prior written permission.
	 *
	 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
	 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
	 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
	 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
	 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
	 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
	 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
	 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
	 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
	 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
	 * POSSIBILITY OF SUCH DAMAGE.
	 *
	 * License 1.0
	 */
package fr.paris.lutece.plugins.solrserver.web;

import java.io.IOException;
import java.util.Map;

import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.request.QueryRequest;
import org.apache.solr.client.solrj.request.SolrQuery;
import org.apache.solr.common.util.NamedList;

import fr.paris.lutece.plugins.solrserver.EmbeddedSolrServerService;
import fr.paris.lutece.portal.service.util.AppLogService;
import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet(name = "SolrServerSelectServlet", urlPatterns = {"/solrserver/solr/select/*"})
public class SolrServerSelectServlet extends HttpServlet
{	
	private static String COLLECTION_NAME = "collection1";
	private static String JSON_CONTENT_TYPE = "application/json; charset=UTF-8";
	private static String SUCCESS_HEADER_VALUE_YES = "yes";
	
	private static String QUERY_PARAMETER = "q";
	
	@Inject
	private EmbeddedSolrServerService embeddedSolrServerService;
	
	/**
	 * Processes select solr documents requests
	 * 
	 * @param request request
	 * @param response response
	 * @throws IOException
	 * @throws SolrServerException
	 */
	protected void processRequest( HttpServletRequest request, HttpServletResponse response ) throws IOException, SolrServerException
	{		
		SolrQuery solrQuery = new SolrQuery( request.getParameter( QUERY_PARAMETER ) );
		Map<String, String[]> parameterMap = request.getParameterMap( );
        for ( Map.Entry<String, String[]> entry : parameterMap.entrySet( ) )
        {
            String key = entry.getKey( );
            if( !QUERY_PARAMETER.equals( key ) )
            {
            	String value = parameterMap.get( key )[0];
            	solrQuery.setParam( value );
            }
        
        }
		
		NamedList<Object> docs = embeddedSolrServerService.getServer( ).request( new QueryRequest( solrQuery ), COLLECTION_NAME );
		
		response.setHeader("Content-Type", JSON_CONTENT_TYPE );
	    response.setHeader("success", SUCCESS_HEADER_VALUE_YES );
		response.getWriter().println( docs.jsonStr( ) );
		response.flushBuffer();
	}
	
	@Override
	public void doGet( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException
	{
		try
		{
		   processRequest( request, response );
	 	}
	    catch( SolrServerException e )
		{
		   AppLogService.error( e.getStackTrace( ), e );
		   throw new ServletException( "Solr error : "+ e.getMessage( ) );
		} 
	}
		
	 @Override
	 public void doPost( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException
	 {
		 try
		 {
			processRequest( request, response );
	 	 }
	     catch( SolrServerException e )
		 {
			AppLogService.error( e.getStackTrace( ), e );
			throw new ServletException( "Solr error : "+ e.getMessage( ) );
		 }
     }
	
	 /**
      * Returns a short description of the servlet.
      * 
      * @return message
     */
	 @Override
	 public String getServletInfo( )
	 {
    	return "Servlet displaying list of documents indexed in solr server";
	 }
}