package org.xbib.graph.export;

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

import static org.xbib.graph.CommonsGraph.export;
import static org.xbib.graph.CommonsGraph.newUndirectedMutableGraph;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.xbib.graph.builder.AbstractGraphConnection;
import org.xbib.graph.model.BaseLabeledVertex;
import org.xbib.graph.model.BaseLabeledWeightedEdge;
import org.xbib.graph.model.UndirectedMutableGraph;

public class ExportTestCase {

    private UndirectedMutableGraph<BaseLabeledVertex, BaseLabeledWeightedEdge<Double>> actual;

    @BeforeMethod
    public void setUp()
    {
        actual =
        newUndirectedMutableGraph( new AbstractGraphConnection<BaseLabeledVertex, BaseLabeledWeightedEdge<Double>>()
        {

            public void connect()
            {
                BaseLabeledVertex start = addVertex( new BaseLabeledVertex( "start" ) );
                BaseLabeledVertex a = addVertex( new BaseLabeledVertex( "a" ) );
                BaseLabeledVertex b = addVertex( new BaseLabeledVertex( "b" ) );
                BaseLabeledVertex goal = addVertex( new BaseLabeledVertex( "goal" ) );

                addEdge( new BaseLabeledWeightedEdge<Double>( "start <-> a", 1.5D ) ).from(start).to( a );
                addEdge( new BaseLabeledWeightedEdge<Double>( "a <-> b", 2D ) ).from(a).to( b );
                addEdge( new BaseLabeledWeightedEdge<Double>( "a <-> goal", 2D ) ).from(a).to( goal );
                addEdge( new BaseLabeledWeightedEdge<Double>( "b <-> goal", 2D ) ).from( b ).to( goal );
            }

        } );
    }

    @AfterMethod
    public void tearDown()
    {
        actual = null;
    }

    @Test
    public void shouldPrintDotFormat()
        throws Exception
    {
        export( actual ).withName( "DotFormatGraph" )
                        .usingDotNotation()
                        .withVertexLabels( new VertexLabelMapper() )
                        .withEdgeWeights( new EdgeWeightMapper() )
                        .withEdgeLabels( new EdgeLabelMapper() )
                        .to( System.out );
    }

    public void shouldPrintGraphML()
        throws Exception
    {
        export( actual ).withName( "GraphMLGraph" )
                        .usingGraphMLFormat()
                        .withVertexLabels( new VertexLabelMapper() )
                        .withEdgeWeights( new EdgeWeightMapper() )
                        .withEdgeLabels( new EdgeLabelMapper() )
                        .to( System.out );
    }

    @Test
    public void shouldPrintGraphMLFormat()
        throws Exception
    {
        export( actual ).usingGraphMLFormat().to( System.out );
    }

}
