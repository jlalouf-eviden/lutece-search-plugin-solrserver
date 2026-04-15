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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.client.solrj.request.UpdateRequest;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.util.NamedList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import fr.paris.lutece.plugins.solrserver.EmbeddedSolrServerService;
import fr.paris.lutece.portal.service.util.AppLogService;
import fr.paris.lutece.portal.web.upload.MultipartHttpServletRequest;
import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

@WebServlet(name = "SolrServerUpdateServlet", urlPatterns = {"/update"})
public class SolrServerUpdateServlet extends HttpServlet
{
	private static String COLLECTION_NAME = "collection1";
	private static String XML_FILE_TYPE = "xml";
	private static String DOC_TAG_NAME = "doc";
	private static String ADD_TAG_NAME = "add";
	private static String DELETE_TAG_NAME = "delete";
	private static String QUERY_TAG_NAME = "query";
	private static String NAME_ATTRIBUTE = "name";
	private static String TMP_SOLR_FILE_NAME = "solr_tmp";
	private static String SUCCESS_HEADER_VALUE_YES = "yes";
	
	private static String JSON_CONTENT_TYPE = "application/json; charset=UTF-8";

	@Inject
	private EmbeddedSolrServerService embeddedSolrServerService;
	
	/**
	 * Processes solr documents update requests
	 * 
	 * @param request request
	 * @param response response
	 * @throws ServletException
	 * @throws IOException
	 * @throws SolrServerException
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 */
	protected void processRequest( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException, SolrServerException, ParserConfigurationException, SAXException
	{
		MultipartHttpServletRequest multipartRequest = ( MultipartHttpServletRequest ) request;
		List<SolrInputDocument> solrDocList = new ArrayList<SolrInputDocument>( );
		String action = null;
		String deleteQuery = null;
		
		for( Part part:  multipartRequest.getParts( ) )
		{
			if(part.getContentType( ) != null && part.getContentType( ).contains( XML_FILE_TYPE ) )
			{
				File file = File.createTempFile( TMP_SOLR_FILE_NAME, null );
		        Files.copy( part.getInputStream( ), file.toPath( ), StandardCopyOption.REPLACE_EXISTING );
	    		    	    		
	    		DocumentBuilder dBuilder = DocumentBuilderFactory.newInstance( ).newDocumentBuilder( );
	    		Document doc = dBuilder.parse( file );
	    		
	    		action = doc.getDocumentElement( ).getNodeName( );
	    		if( ADD_TAG_NAME.equals( action ) )
	    		{
	    			NodeList docList = doc.getElementsByTagName( DOC_TAG_NAME );
		    		for(int i = 0; i < docList.getLength( ); i++)
		    		{
		    			Node docNode = docList.item(i);
		    			
		    			if( docNode.getNodeType( ) == Node.ELEMENT_NODE )
		    			{
		    				SolrInputDocument solrDoc = new SolrInputDocument( );
		    				Element docElement = ( Element ) docNode;
		    				NodeList fieldsList = docElement.getChildNodes( );

		    	            for (int fieldIdx = 0; fieldIdx < fieldsList.getLength( ); fieldIdx++ ) 
		    	            {
		    	                Node fieldNode = fieldsList.item(fieldIdx);

		    	                if (fieldNode.getNodeType() == Node.ELEMENT_NODE)
		    	                {
		    	                    Element fieldElement = ( Element ) fieldNode;
		    	                    solrDoc.addField( fieldElement.getAttribute( NAME_ATTRIBUTE ), fieldElement.getTextContent( ) );
		    	                }
		    	            }

		    	            solrDocList.add( solrDoc );
		    			}
		    		}
	    		} 
	    		else if( DELETE_TAG_NAME.equals( action ) )
	    		{	  
	    			NodeList queryList = doc.getElementsByTagName( QUERY_TAG_NAME );
	    			deleteQuery = queryList.item( 0 ).getFirstChild( ).getNodeValue( );
	    		}

		        part.delete( );	        
			}
		}
		if( ADD_TAG_NAME.equals( action ) )
		{
			EmbeddedSolrServer server = embeddedSolrServerService.getServer( );
			UpdateResponse solrResp = server.add( solrDocList );
			server.commit( );
			
			response.setHeader( "Content-Type", JSON_CONTENT_TYPE );
		    response.setHeader( "success", SUCCESS_HEADER_VALUE_YES);
			response.getWriter( ).println( solrResp.getResponse( ).jsonStr( ) );
			response.flushBuffer( );
		}	 
		else if( DELETE_TAG_NAME.equals( action ) )
		{
			if( deleteQuery != null )
			{
				EmbeddedSolrServer server = embeddedSolrServerService.getServer( );
				UpdateRequest updateRequest = new UpdateRequest( );
				updateRequest = updateRequest.deleteByQuery(deleteQuery);
				NamedList<Object> objects = server.request( updateRequest, COLLECTION_NAME );
				server.commit( );
				
				response.setHeader("Content-Type", JSON_CONTENT_TYPE );
			    response.setHeader("success", SUCCESS_HEADER_VALUE_YES );
			    response.getWriter().println( objects.jsonStr( ) );
				response.flushBuffer();
			}			
		}
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
		catch( SAXException e )
		{
		   AppLogService.error( e.getStackTrace( ), e );
		   throw new ServletException( "SAX error : "+ e.getMessage( ) );
		}
		catch( ParserConfigurationException e )
		{
		   AppLogService.error( e.getStackTrace( ), e );
		   throw new ServletException( "Parsing error : "+ e.getMessage( ) );
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
		catch( SAXException e )
		{
		   AppLogService.error( e.getStackTrace( ), e );
		   throw new ServletException( "SAX error : "+ e.getMessage( ) );
		}
		catch( ParserConfigurationException e )
		{
		   AppLogService.error( e.getStackTrace( ), e );
		   throw new ServletException( "Parsing error : "+ e.getMessage( ) );
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
		return "Servlet inserting documents in solr server";
	}	   
}