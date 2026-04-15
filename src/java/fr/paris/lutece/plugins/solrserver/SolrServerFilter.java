/*
 * Copyright (c) 2002-2014, Mairie de Paris
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
package fr.paris.lutece.plugins.solrserver;

import java.io.IOException;

import org.apache.solr.servlet.SolrDispatchFilter;

import fr.paris.lutece.portal.business.user.AdminUser;
import fr.paris.lutece.portal.service.admin.AdminUserService;
import fr.paris.lutece.portal.service.util.AppPropertiesService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;


public class SolrServerFilter extends SolrDispatchFilter
{
    public static final String SOLR_DATA_DIR = "solr.data.dir";
    public static final String SOLR_LOGS_DIR = "solr.logs.dir";
    public static final String SOLR_HOME_LABEL = "solr.solr.home";
    public static final String SOLR_URI = AppPropertiesService.getProperty( "solrserver.solr.uri" );
    public static final String SOLR_URI_UPDATE = AppPropertiesService.getProperty( "solrserver.solr.uri.update" );
    public static final String SOLR_URI_SELECT = AppPropertiesService.getProperty( "solrserver.solr.uri.select" );
    public static final String SOLR_URI_AUTOCOMPLETE = AppPropertiesService.getProperty( "solrserver.solr.uri.autoComplete" );
    
    public static final String SOLR_HOME = AppPropertiesService.getProperty( "solrserver.solr.home" );
    public static final String SOLR_ABSOLUTE_DATA = AppPropertiesService.getProperty( "solrserver.solr.absolute.data" );
    public static final String SOLR_RELATIVE_DATA = AppPropertiesService.getProperty( "solrserver.solr.relative.data" );
    public static final String SOLR_ADMIN_CLIENT = AppPropertiesService.getProperty("solrserver.solr.host.client", "127.0.0.1" );

    private static final String SELECT_URI = "/collection1/select/";
    private static final String UPDATE_URI = "/collection1/update/";
    private static final String SUGGEST_URI = "/collection1/suggest/";
    
    private static FilterConfig _filterConfig;
    private static boolean init;
    
    @Override
    public  void init( FilterConfig filterConfig ) throws ServletException
    {
        String realPath = filterConfig.getServletContext(  ).getRealPath( "/" );

        System.setProperty( SOLR_HOME_LABEL, SOLR_HOME );
        
        System.setProperty( SOLR_LOGS_DIR, SOLR_HOME+"/logs" );

        if ( ( SOLR_ABSOLUTE_DATA == null ) || ( SOLR_ABSOLUTE_DATA.length(  ) == 0 ) )
        {
            System.setProperty( SOLR_DATA_DIR, realPath +"/"+ SOLR_RELATIVE_DATA );
        }
        else
        {
            System.setProperty( SOLR_DATA_DIR, SOLR_ABSOLUTE_DATA );
        }

    	_filterConfig = filterConfig;
    }
    
    @Override   
    public  void doFilter( ServletRequest request, ServletResponse response, FilterChain chain )
            throws IOException, ServletException
    { 	
    	if(init == false)
    	{
    		super.init( _filterConfig );
    		init = true;
    	}
        String strURI = ( (HttpServletRequest) request ).getRequestURI(  );
        boolean bCallSolr = false;
      
        if ( strURI.indexOf( SOLR_URI_UPDATE ) > 0 )
        {
            AdminUser adminUser = AdminUserService.getAdminUser( (HttpServletRequest) request );
            String strRemoteAddr = ( (HttpServletRequest) request ).getRemoteAddr(  );

            if ( ( adminUser != null ) || ( strRemoteAddr.compareTo( SOLR_ADMIN_CLIENT ) == 0 ) )
            {
                bCallSolr = true;
            }
        }
        else if ( strURI.indexOf( SOLR_URI_SELECT ) > 0 )
        {
            bCallSolr = true;
        }
        else if ( strURI.indexOf( SOLR_URI_AUTOCOMPLETE ) > 0 )
        {
            bCallSolr = true;
        }

        if ( bCallSolr ) {
            request = new HttpServletRequestWrapper( (HttpServletRequest) request ) 
            {
                @Override 
                public String getServletPath( ) 
                {
                	String path = null;
                	String url = ( (HttpServletRequest) getRequest( ) ).getRequestURL( ).toString( );
                	if( url.contains( "select" ) )
                	{
                		 path = SELECT_URI;
                	}
                	else if( url.contains( "update" ) )
                	{
                		path = UPDATE_URI;
                	}
                	else if( url.contains( "suggest" ) )
                	{
                		path = SUGGEST_URI;
                	}

                    return path;
                };
                
                @Override public String getPathInfo( ) 
                {
                    return null;
                };
            };

            super.doFilter( request, response, chain );
        }
        
    }
    
    @Override
    public  void destroy(  )
    {
        super.destroy(  );
    }
}