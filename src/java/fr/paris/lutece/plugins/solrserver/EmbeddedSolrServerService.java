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
package fr.paris.lutece.plugins.solrserver;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Properties;

import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.core.CoreContainer;
import org.apache.solr.core.NodeConfig;
import org.apache.solr.core.SolrXmlConfig;

import fr.paris.lutece.portal.service.util.AppPropertiesService;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;

@ApplicationScoped
@Named("embeddedSolrServerService")
public class EmbeddedSolrServerService
{
    public static final String SOLR_HOME_LABEL = "solr.solr.home";
    public static final String SOLR_HOME = AppPropertiesService.getProperty( "solrserver.solr.home" );
    public static final String COLLECTION_NAME = "collection1";
	
	private CoreContainer _coreContainer;
    private EmbeddedSolrServer _server;
	
    @PostConstruct
    void init( )
    {
		System.setProperty( SOLR_HOME_LABEL, SOLR_HOME );
        NodeConfig nodeConfig = SolrXmlConfig.fromSolrHome( Path.of( System.getProperty( SOLR_HOME_LABEL ) ), new Properties( ) );
        
        _coreContainer = new CoreContainer( nodeConfig );
        _coreContainer.load( );
        
        _server = new EmbeddedSolrServer( _coreContainer, COLLECTION_NAME );
    }

    /**
     * Returns embedded solr server instance
     * 
     * @return embedded solr server instance
     */
	public EmbeddedSolrServer getServer( ) 
	{
        return _server;
    }

	/**
	 * shutdown method
	 * 
	 * @throws IOException
	 */
    public void shutdown( ) throws IOException 
    {
        if ( _server != null ) 
        {
        	_server.close( );
        }
        if  ( _coreContainer != null ) 
        {
        	_coreContainer.shutdown( );
        }
    }
}