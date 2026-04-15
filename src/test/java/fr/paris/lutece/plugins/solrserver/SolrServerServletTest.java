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

	import org.apache.solr.common.SolrInputDocument;
import org.eclipse.jetty.util.StringUtil;
import org.junit.jupiter.api.Test;

import fr.paris.lutece.plugins.solrserver.web.SolrServerSelectServlet;
import fr.paris.lutece.plugins.solrserver.web.SolrServerUpdateServlet;
import fr.paris.lutece.portal.service.upload.MultipartAsyncUploadHandler;
import fr.paris.lutece.portal.service.upload.MultipartHandler;
import fr.paris.lutece.portal.web.upload.MultipartHttpServletRequest;
import fr.paris.lutece.test.LuteceTestCase;
import fr.paris.lutece.test.mocks.MockHttpServletRequest;
import fr.paris.lutece.test.mocks.MockHttpServletResponse;
import fr.paris.lutece.test.mocks.MockPart;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

	public class SolrServerServletTest extends LuteceTestCase
	{
		private static String QUERY_PARAMETER = "q";
		private static String QUERY_VALUE = "*:*";
		
		private static String WRITER_TYPE_PARAMETER = "wt";
		private static String WRITER_TYPE_VALUE = "json";
		
		private static String INDENT_PARAMETER = "indent";
		private static String INDENT_TYPE_VALUE = "on";
		
		@Inject
		private EmbeddedSolrServerService embeddedSolrServerService;
		
		@Inject
		@MultipartAsyncUploadHandler
		private MultipartHandler _multipartHandler;
		
	    @Inject
	    private SolrServerServletSelectTestWrapper _solrServerSelectServlet;
	    
	    @Inject
	    private SolrServerServletUpdateTestWrapper _solrServerUpdateServlet;
	        
	    @Test
	    public void testUpdateDeleteDocs( ) throws Exception
	    {
	    	// Inserting test data
	        SolrInputDocument doc1 = new SolrInputDocument( );
	        doc1.addField( "uid", "doc-test-unit-delete-1" );
	        doc1.addField("content", "Ceci est un contenu de test indexé dans Solr.");
	        
	        // Document 2
	        SolrInputDocument doc2 = new SolrInputDocument( );
	        doc2.addField( "uid", "doc-test-unit-delete-2" );
	        doc2.addField("content", "Ceci est un contenu de test indexé dans Solr.");
	        
	        embeddedSolrServerService.getServer( ).add( doc1 );
	        embeddedSolrServerService.getServer( ).add( doc2 );
	        embeddedSolrServerService.getServer( ).commit( );
	        
	        assertNotNull( embeddedSolrServerService.getServer( ).getById( "doc-test-unit-delete-1" ) );
	        assertNotNull( embeddedSolrServerService.getServer( ).getById( "doc-test-unit-delete-2" ) );
			
	    	
	    	// Building the multipart request
			MockHttpServletRequest request = new MockHttpServletRequest( );
			request.addPart( new MockPart( "testFile", "application/xml", "testFile", getTestDeleteFile( ).getBytes() ) );
			MultipartHttpServletRequest multiPartRequest = _multipartHandler.handle( request, false );			
			
			// Testing the servlet
			MockHttpServletResponse response = new MockHttpServletResponse( );
			_solrServerUpdateServlet.doPost( multiPartRequest, response );

	        assertFalse( StringUtil.isEmpty( response.getContentAsString( ) ) );        
	        assertNull( embeddedSolrServerService.getServer( ).getById( "doc-test-unit-delete-1" ) );
	        assertNull( embeddedSolrServerService.getServer( ).getById( "doc-test-unit-delete-2" ) );
	    }
	    
		@Test
	    public void testUpdateAddDocs( ) throws Exception
	    {
			
			// Building the multipart request
			MockHttpServletRequest request = new MockHttpServletRequest( );
			request.addPart( new MockPart( "testFile", "application/xml", "testFile", getTestAddFile( ).getBytes() ) );
			MultipartHttpServletRequest multiPartRequest = _multipartHandler.handle( request, false );			
			
			// Testing the servlet
			MockHttpServletResponse response = new MockHttpServletResponse( );
			_solrServerUpdateServlet.doPost( multiPartRequest, response );

	        assertFalse( StringUtil.isEmpty( response.getContentAsString( ) ) );
	        assertNotNull( embeddedSolrServerService.getServer( ).getById( "doc-test-unit-update-1" ) );
	        assertNotNull( embeddedSolrServerService.getServer( ).getById( "doc-test-unit-update-2" ) );
	        assertNull( embeddedSolrServerService.getServer( ).getById( "doc-test-unit-2-not-existing" ) );
	        
	        // Deleting test data
	        embeddedSolrServerService.getServer( ).deleteById( "doc-test-unit-update-1" );
	        embeddedSolrServerService.getServer( ).deleteById( "doc-test-unit-update-2" );
			embeddedSolrServerService.getServer( ).commit( );
	    } 
		
		@Test
	    public void testSelect( ) throws Exception
	    {
	        // Inserting test data
	        SolrInputDocument doc1 = new SolrInputDocument( );
	        doc1.addField( "uid", "doc-test-unit-select-1" );
	        doc1.addField("content", "Ceci est un contenu de test indexé dans Solr.");
	        
	        // Document 2
	        SolrInputDocument doc2 = new SolrInputDocument( );
	        doc2.addField( "uid", "doc-test-unit-select-2" );
	        doc2.addField("content", "Ceci est un contenu de test indexé dans Solr.");
	        
	        embeddedSolrServerService.getServer( ).add( doc1 );
	        embeddedSolrServerService.getServer( ).add( doc2 );
	        embeddedSolrServerService.getServer( ).commit( );

	        // Testing the servlet
	        MockHttpServletRequest request = new MockHttpServletRequest( );
	        request.setParameter( QUERY_PARAMETER, QUERY_VALUE );
	        request.setParameter( WRITER_TYPE_PARAMETER, WRITER_TYPE_VALUE );
	        request.setParameter( INDENT_PARAMETER, INDENT_TYPE_VALUE );
	        
	        MockHttpServletResponse response = new MockHttpServletResponse( );
	        _solrServerSelectServlet.doGet( request, response );

	        String strResponseJson = response.getContentAsString( );

	        assertFalse( StringUtil.isEmpty(strResponseJson) );
	        assertTrue( strResponseJson.contains( "doc-test-unit-select-1" ) );
	        assertTrue( strResponseJson.contains( "doc-test-unit-select-2" ) );
	        
	        // Deleting test data
	        embeddedSolrServerService.getServer( ).deleteById( "doc-test-unit-select-1" );
	        embeddedSolrServerService.getServer( ).deleteById( "doc-test-unit-select-2" );
			embeddedSolrServerService.getServer( ).commit( );
	    }
		
	    private String getTestDeleteFile( )
		{
	    	return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
	    			+ "<delete>\n"
	    			+ "	<query>*:*</query>\n"
	    			+ "</delete>";
		}
	    
		private String getTestAddFile( )
		{
			return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
					+ "<add>\n"
					+ "  <doc>\n"
					+ "    <!-- Champ unique obligatoire -->\n"
					+ "    <field name=\"uid\">doc-test-unit-update-1</field>\n"
					+ "\n"
					+ "    <!-- Champs simples -->\n"
					+ "    <field name=\"url\">https://www.exemple.com/article/123</field>\n"
					+ "    <field name=\"date\">2025-05-12T10:30:00Z</field>\n"
					+ "    <field name=\"title\">Titre d'exemple pour l’index Solr</field>\n"
					+ "    <field name=\"content\">\n"
					+ "      Ceci est un contenu de test indexé dans Solr pour vérifier la configuration du schéma.\n"
					+ "    </field>\n"
					+ "    <field name=\"site\">site-exemple</field>\n"
					+ "    <field name=\"summary\">\n"
					+ "      Résumé court du contenu pour démonstration.\n"
					+ "    </field>\n"
					+ "    <field name=\"type\">article</field>\n"
					+ "    <field name=\"role\">public</field>\n"
					+ "    <field name=\"file_content\">\n"
					+ "      Contenu texte extrait d’un fichier PDF ou autre.\n"
					+ "    </field>\n"
					+ "    <field name=\"xml_content\">\n"
					+ "      &lt;meta&gt;Exemple de contenu XML stocké en texte&lt;/meta&gt;\n"
					+ "    </field>\n"
					+ "    <field name=\"hiedate\">2025-05-12</field>\n"
					+ "    <field name=\"metadata\">auteur=Dupont;lang=fr;version=1</field>\n"
					+ "    <field name=\"document_portlet_id\">portlet-42</field>\n"
					+ "    <field name=\"id_resource\">res-123</field>\n"
					+ "\n"
					+ "    <!-- Champ multi-valué -->\n"
					+ "    <field name=\"categorie\">actualité</field>\n"
					+ "    <field name=\"categorie\">technique</field>\n"
					+ "\n"
					+ "    <!-- Champs dynamiques *_string -->\n"
					+ "    <field name=\"custom1_string\">valeur personnalisée 1</field>\n"
					+ "    <field name=\"statut_string\">publie</field>\n"
					+ "\n"
					+ "    <!-- Champs dynamiques *_text -->\n"
					+ "    <field name=\"tags_text\">solr schéma index</field>\n"
					+ "    <field name=\"description_longue_text\">\n"
					+ "      Texte plus long stocké dans un champ dynamique de type text.\n"
					+ "    </field>\n"
					+ "\n"
					+ "    <!-- Champs dynamiques *_date -->\n"
					+ "    <field name=\"publication_date\">2025-05-12T10:30:00Z</field>\n"
					+ "    <field name=\"modification_date\">2025-05-15T08:15:00Z</field>\n"
					+ "\n"
					+ "    <!-- Champs dynamiques *_long -->\n"
					+ "    <field name=\"taille_document_long\">2048</field>\n"
					+ "    <field name=\"nb_vues_long\">12345</field>\n"
					+ "\n"
					+ "    <!-- Champ dynamique multi-valué *_list -->\n"
					+ "    <field name=\"mots_cles_list\">solr</field>\n"
					+ "    <field name=\"mots_cles_list\">lutece</field>\n"
					+ "    <field name=\"mots_cles_list\">indexation</field>\n"
					+ "\n"
					+ "    <!-- Champs dynamiques géo -->\n"
					+ "    <!-- GeoJSON stocké uniquement -->\n"
					+ "    <field name=\"position_geojson\">\n"
					+ "      {\"type\":\"Point\",\"coordinates\":[2.3522,48.8566]}\n"
					+ "    </field>\n"
					+ "\n"
					+ "    <!-- Couche géo (nom, code, etc.) -->\n"
					+ "    <field name=\"zone_geolayer\">FR-IDF</field>\n"
					+ "\n"
					+ "    <!-- Champ géoloc (lat,lon) -->\n"
					+ "    <field name=\"adresse_geoloc\">48.8566,2.3522</field>\n"
					+ "\n"
					+ "    <!-- Coordonnées séparées -->\n"
					+ "    <field name=\"adresse_geoloc_0_coordinate\">48.8566</field>\n"
					+ "    <field name=\"adresse_geoloc_1_coordinate\">2.3522</field>\n"
					+ "\n"
					+ "    <!-- Champ dynamique multi-valué *_list_date -->\n"
					+ "    <field name=\"periodes_list_date\">2025-05-01T00:00:00Z</field>\n"
					+ "    <field name=\"periodes_list_date\">2025-05-31T23:59:59Z</field>\n"
					+ "\n"
					+ "  </doc>\n"
					+ "  <doc>\n"
					+ "    <!-- Champ unique obligatoire -->\n"
					+ "    <field name=\"uid\">doc-test-unit-update-2</field>\n"
					+ "\n"
					+ "    <!-- Champs simples -->\n"
					+ "    <field name=\"url\">https://www.exemple.com/article/123</field>\n"
					+ "    <field name=\"date\">2025-05-12T10:30:00Z</field>\n"
					+ "    <field name=\"title\">Titre d'exemple pour l’index Solr</field>\n"
					+ "    <field name=\"content\">\n"
					+ "      Ceci est un contenu de test indexé dans Solr pour vérifier la configuration du schéma.\n"
					+ "    </field>\n"
					+ "    <field name=\"site\">site-exemple</field>\n"
					+ "    <field name=\"summary\">\n"
					+ "      Résumé court du contenu pour démonstration.\n"
					+ "    </field>\n"
					+ "    <field name=\"type\">article</field>\n"
					+ "    <field name=\"role\">public</field>\n"
					+ "    <field name=\"file_content\">\n"
					+ "      Contenu texte extrait d’un fichier PDF ou autre.\n"
					+ "    </field>\n"
					+ "    <field name=\"xml_content\">\n"
					+ "      &lt;meta&gt;Exemple de contenu XML stocké en texte&lt;/meta&gt;\n"
					+ "    </field>\n"
					+ "    <field name=\"hiedate\">2025-05-12</field>\n"
					+ "    <field name=\"metadata\">auteur=Dupont;lang=fr;version=1</field>\n"
					+ "    <field name=\"document_portlet_id\">portlet-42</field>\n"
					+ "    <field name=\"id_resource\">res-123</field>\n"
					+ "\n"
					+ "    <!-- Champ multi-valué -->\n"
					+ "    <field name=\"categorie\">actualité</field>\n"
					+ "    <field name=\"categorie\">technique</field>\n"
					+ "\n"
					+ "    <!-- Champs dynamiques *_string -->\n"
					+ "    <field name=\"custom1_string\">valeur personnalisée 1</field>\n"
					+ "    <field name=\"statut_string\">publie</field>\n"
					+ "\n"
					+ "    <!-- Champs dynamiques *_text -->\n"
					+ "    <field name=\"tags_text\">solr schéma index</field>\n"
					+ "    <field name=\"description_longue_text\">\n"
					+ "      Texte plus long stocké dans un champ dynamique de type text.\n"
					+ "    </field>\n"
					+ "\n"
					+ "    <!-- Champs dynamiques *_date -->\n"
					+ "    <field name=\"publication_date\">2025-05-12T10:30:00Z</field>\n"
					+ "    <field name=\"modification_date\">2025-05-15T08:15:00Z</field>\n"
					+ "\n"
					+ "    <!-- Champs dynamiques *_long -->\n"
					+ "    <field name=\"taille_document_long\">2048</field>\n"
					+ "    <field name=\"nb_vues_long\">12345</field>\n"
					+ "\n"
					+ "    <!-- Champ dynamique multi-valué *_list -->\n"
					+ "    <field name=\"mots_cles_list\">solr</field>\n"
					+ "    <field name=\"mots_cles_list\">lutece</field>\n"
					+ "    <field name=\"mots_cles_list\">indexation</field>\n"
					+ "\n"
					+ "    <!-- Champs dynamiques géo -->\n"
					+ "    <!-- GeoJSON stocké uniquement -->\n"
					+ "    <field name=\"position_geojson\">\n"
					+ "      {\"type\":\"Point\",\"coordinates\":[2.3522,48.8566]}\n"
					+ "    </field>\n"
					+ "\n"
					+ "    <!-- Couche géo (nom, code, etc.) -->\n"
					+ "    <field name=\"zone_geolayer\">FR-IDF</field>\n"
					+ "\n"
					+ "    <!-- Champ géoloc (lat,lon) -->\n"
					+ "    <field name=\"adresse_geoloc\">48.8566,2.3522</field>\n"
					+ "\n"
					+ "    <!-- Coordonnées séparées -->\n"
					+ "    <field name=\"adresse_geoloc_0_coordinate\">48.8566</field>\n"
					+ "    <field name=\"adresse_geoloc_1_coordinate\">2.3522</field>\n"
					+ "\n"
					+ "    <!-- Champ dynamique multi-valué *_list_date -->\n"
					+ "    <field name=\"periodes_list_date\">2025-05-01T00:00:00Z</field>\n"
					+ "    <field name=\"periodes_list_date\">2025-05-31T23:59:59Z</field>\n"
					+ "\n"
					+ "  </doc>\n"
					+ "</add>\n"
					+ "";
		}
		
		@ApplicationScoped
    	public static class SolrServerServletSelectTestWrapper extends SolrServerSelectServlet
	    {
			
	    }
	    
	    @ApplicationScoped
	    public static class SolrServerServletUpdateTestWrapper extends SolrServerUpdateServlet
	    {

	    }
	}