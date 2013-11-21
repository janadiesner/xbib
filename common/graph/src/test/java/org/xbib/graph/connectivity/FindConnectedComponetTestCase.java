package org.xbib.graph.connectivity;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import static org.xbib.graph.CommonsGraph.findConnectedComponent;
import static org.xbib.graph.CommonsGraph.newUndirectedMutableGraph;

import java.util.Collection;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.xbib.graph.Graph;
import org.xbib.graph.builder.AbstractGraphConnection;
import org.xbib.graph.model.BaseLabeledEdge;
import org.xbib.graph.model.BaseLabeledVertex;
import org.xbib.graph.model.BaseLabeledWeightedEdge;
import org.xbib.graph.model.UndirectedMutableGraph;

/**
 */
public final class FindConnectedComponetTestCase
{

    @Test(expectedExceptions = NullPointerException.class)
    public void verifyNullGraph()
    {
        findConnectedComponent( (Graph<BaseLabeledVertex, BaseLabeledWeightedEdge<Double>>) null ).includingAllVertices().applyingMinimumSpanningTreeAlgorithm();
    }

    @Test
    public void verifyEmptyGraph()
    {
        UndirectedMutableGraph<BaseLabeledVertex, BaseLabeledEdge> graph =
            new UndirectedMutableGraph<BaseLabeledVertex, BaseLabeledEdge>();

        Collection<List<BaseLabeledVertex>> c =
            findConnectedComponent( graph ).includingAllVertices().applyingMinimumSpanningTreeAlgorithm();
        Assert.assertNotNull(c);
        Assert.assertEquals( 0, c.size() );
    }

    @Test
    public void verifyNullVerticesGraph()
    {
        UndirectedMutableGraph<BaseLabeledVertex, BaseLabeledEdge> graph =
            newUndirectedMutableGraph( new AbstractGraphConnection<BaseLabeledVertex, BaseLabeledEdge>()
            {

                public void connect()
                {
                    addVertex( new BaseLabeledVertex( "B" ) );
                    addVertex( new BaseLabeledVertex( "C" ) );
                }

            } );
        Collection<List<BaseLabeledVertex>> c =
            findConnectedComponent( graph ).includingVertices().applyingMinimumSpanningTreeAlgorithm();
        Assert.assertNotNull( c );
        Assert.assertEquals( 0, c.size() );
    }

    @Test
    public void verifyConnectedComponents()
    {
        final BaseLabeledVertex a = new BaseLabeledVertex( "A" );

        UndirectedMutableGraph<BaseLabeledVertex, BaseLabeledEdge> graph =
        newUndirectedMutableGraph( new AbstractGraphConnection<BaseLabeledVertex, BaseLabeledEdge>()
        {

            public void connect()
            {
                addVertex( a );
                addVertex( new BaseLabeledVertex( "B" ) );
                addVertex( new BaseLabeledVertex( "C" ) );
                addVertex( new BaseLabeledVertex( "D" ) );
                addVertex( new BaseLabeledVertex( "E" ) );
                addVertex( new BaseLabeledVertex( "F" ) );
                addVertex( new BaseLabeledVertex( "G" ) );
                addVertex( new BaseLabeledVertex( "H" ) );
            }

        } );

        Collection<List<BaseLabeledVertex>> c = findConnectedComponent( graph ).includingAllVertices().applyingMinimumSpanningTreeAlgorithm();

        Assert.assertNotNull( c );
        Assert.assertFalse( c.isEmpty() );
        Assert.assertEquals( 8, c.size() );
    }

    @Test
    public void verifyConnectedComponents2()
    {
        final BaseLabeledVertex a = new BaseLabeledVertex( "A" );

        UndirectedMutableGraph<BaseLabeledVertex, BaseLabeledEdge> graph =
        newUndirectedMutableGraph( new AbstractGraphConnection<BaseLabeledVertex, BaseLabeledEdge>()
        {

            public void connect()
            {
                addVertex( a );
                BaseLabeledVertex b = addVertex( new BaseLabeledVertex( "B" ) );
                BaseLabeledVertex c = addVertex( new BaseLabeledVertex( "C" ) );
                BaseLabeledVertex d = addVertex( new BaseLabeledVertex( "D" ) );
                BaseLabeledVertex e = addVertex( new BaseLabeledVertex( "E" ) );
                BaseLabeledVertex f = addVertex( new BaseLabeledVertex( "F" ) );
                BaseLabeledVertex g = addVertex( new BaseLabeledVertex( "G" ) );
                BaseLabeledVertex h = addVertex( new BaseLabeledVertex( "H" ) );

                addEdge( new BaseLabeledEdge( "A -> F" ) ).from( a ).to( f );
                addEdge( new BaseLabeledEdge( "A -> B" ) ).from( a ).to( b );
                addEdge( new BaseLabeledEdge( "B -> F" ) ).from( b ).to( f );
                addEdge( new BaseLabeledEdge( "C -> G" ) ).from( c ).to( g );
                addEdge( new BaseLabeledEdge( "D -> G" ) ).from( d ).to( g );
                addEdge( new BaseLabeledEdge( "E -> F" ) ).from( e ).to( f );
                addEdge( new BaseLabeledEdge( "H -> C" ) ).from( h ).to( c );
            }

        } );

        Collection<List<BaseLabeledVertex>> c = findConnectedComponent( graph ).includingAllVertices().applyingMinimumSpanningTreeAlgorithm();

        Assert.assertNotNull( c );
        Assert.assertFalse( c.isEmpty() );
        Assert.assertEquals( 2, c.size() );
    }

    @Test
    public void verifyConnectedComponents3()
    {
        final BaseLabeledVertex a = new BaseLabeledVertex( "A" );

        UndirectedMutableGraph<BaseLabeledVertex, BaseLabeledEdge> graph =
        newUndirectedMutableGraph( new AbstractGraphConnection<BaseLabeledVertex, BaseLabeledEdge>()
        {

            public void connect()
            {
                addVertex( a );
                BaseLabeledVertex b = addVertex( new BaseLabeledVertex( "B" ) );
                BaseLabeledVertex c = addVertex( new BaseLabeledVertex( "C" ) );

                addEdge( new BaseLabeledEdge( "A -> B" ) ).from( a ).to( b );
                addEdge( new BaseLabeledEdge( "B -> C" ) ).from( b ).to( c );
                addEdge( new BaseLabeledEdge( "C -> A" ) ).from( c ).to( a );
            }

        } );

        Collection<List<BaseLabeledVertex>> c = findConnectedComponent( graph ).includingAllVertices().applyingMinimumSpanningTreeAlgorithm();

        Assert.assertNotNull( c );
        Assert.assertFalse( c.isEmpty() );
        Assert.assertEquals( 1, c.size() );
    }

    @Test
    public void verifyConnectedComponentsIncludingVertices()
    {
        final BaseLabeledVertex a = new BaseLabeledVertex( "A" );

        UndirectedMutableGraph<BaseLabeledVertex, BaseLabeledEdge> graph =
        newUndirectedMutableGraph( new AbstractGraphConnection<BaseLabeledVertex, BaseLabeledEdge>()
        {

            public void connect()
            {
                addVertex( a );
                BaseLabeledVertex b = addVertex( new BaseLabeledVertex( "B" ) );
                BaseLabeledVertex c = addVertex( new BaseLabeledVertex( "C" ) );
                BaseLabeledVertex d = addVertex( new BaseLabeledVertex( "D" ) );
                BaseLabeledVertex e = addVertex( new BaseLabeledVertex( "E" ) );
                BaseLabeledVertex f = addVertex( new BaseLabeledVertex( "F" ) );
                BaseLabeledVertex g = addVertex( new BaseLabeledVertex( "G" ) );
                BaseLabeledVertex h = addVertex( new BaseLabeledVertex( "H" ) );

                addEdge( new BaseLabeledEdge( "A -> F" ) ).from( a ).to( f );
                addEdge( new BaseLabeledEdge( "A -> B" ) ).from( a ).to( b );
                addEdge( new BaseLabeledEdge( "B -> F" ) ).from( b ).to( f );
                addEdge( new BaseLabeledEdge( "C -> G" ) ).from( c ).to( g );
                addEdge( new BaseLabeledEdge( "D -> G" ) ).from( d ).to( g );
                addEdge( new BaseLabeledEdge( "E -> F" ) ).from( e ).to( f );
                addEdge( new BaseLabeledEdge( "H -> C" ) ).from( h ).to( c );
            }

        } );

        Collection<List<BaseLabeledVertex>> coll = findConnectedComponent( graph ).includingVertices( a ).applyingMinimumSpanningTreeAlgorithm();

        Assert.assertNotNull( coll );
        Assert.assertFalse( coll.isEmpty() );
        Assert.assertEquals( 1, coll.size() );
    }

    @Test
    public void verifyConnectedComponentsIncludingVertices2()
    {
        final BaseLabeledVertex a = new BaseLabeledVertex( "A" );
        final BaseLabeledVertex e = new BaseLabeledVertex( "E" );

        UndirectedMutableGraph<BaseLabeledVertex, BaseLabeledEdge> graph =
        newUndirectedMutableGraph( new AbstractGraphConnection<BaseLabeledVertex, BaseLabeledEdge>()
        {

            public void connect()
            {
                addVertex( a );
                addVertex( new BaseLabeledVertex( "B" ) );
                addVertex( new BaseLabeledVertex( "C" ) );
                addVertex( new BaseLabeledVertex( "D" ) );
                addVertex( e );
                addVertex( new BaseLabeledVertex( "F" ) );
                addVertex( new BaseLabeledVertex( "G" ) );
                addVertex( new BaseLabeledVertex( "H" ) );

            }

        } );

        Collection<List<BaseLabeledVertex>> coll = findConnectedComponent( graph ).includingVertices( a, e ).applyingMinimumSpanningTreeAlgorithm();

        Assert.assertNotNull( coll );
        Assert.assertFalse( coll.isEmpty() );
        Assert.assertEquals( 2, coll.size() );
    }

}
