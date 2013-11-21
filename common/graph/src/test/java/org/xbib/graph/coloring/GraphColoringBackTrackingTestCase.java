package org.xbib.graph.coloring;

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

import static org.xbib.graph.CommonsGraph.coloring;
import static org.xbib.graph.CommonsGraph.newUndirectedMutableGraph;
import static org.xbib.graph.utils.GraphUtils.buildBipartedGraph;
import static org.xbib.graph.utils.GraphUtils.buildCompleteGraph;
import static org.xbib.graph.utils.GraphUtils.buildCrownGraph;
import static org.xbib.graph.utils.GraphUtils.buildSudokuGraph;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.logging.Logger;

import org.testng.Assert;
import org.testng.annotations.Test;

import org.xbib.graph.UndirectedGraph;
import org.xbib.graph.builder.AbstractGraphConnection;
import org.xbib.graph.model.BaseLabeledEdge;
import org.xbib.graph.model.BaseLabeledVertex;
import org.xbib.graph.model.BaseLabeledWeightedEdge;
import org.xbib.graph.model.UndirectedMutableGraph;

/**
 *
 */
public class GraphColoringBackTrackingTestCase
    extends AbstractColoringTest
{

    @Test( expectedExceptions = NullPointerException.class )
    public void testNullGraph()
    {
        coloring( (UndirectedGraph<BaseLabeledVertex, BaseLabeledWeightedEdge<Double>>) null ).withColors( null ).applyingBackTrackingAlgorithm();
    }

    @Test( expectedExceptions = NullPointerException.class )
    public void testNullColorGraph()
    {
        UndirectedMutableGraph<BaseLabeledVertex, BaseLabeledEdge> g =
            new UndirectedMutableGraph<BaseLabeledVertex, BaseLabeledEdge>();
        coloring( g ).withColors( null ).applyingBackTrackingAlgorithm();
    }

    @Test
    public void testEmptyGraph()
    {
        UndirectedMutableGraph<BaseLabeledVertex, BaseLabeledEdge> g =
            new UndirectedMutableGraph<BaseLabeledVertex, BaseLabeledEdge>();

        ColoredVertices<BaseLabeledVertex, Integer> coloredVertices =
            coloring( g ).withColors( createColorsList( 1 ) ).applyingBackTrackingAlgorithm();
        Assert.assertNotNull(coloredVertices);
        Assert.assertEquals(0, coloredVertices.getRequiredColors());
    }

    @Test(expectedExceptions =NotEnoughColorsException.class)
    public void testNotEnoughtColorGraph()
        throws NotEnoughColorsException
    {
        final BaseLabeledVertex two = new BaseLabeledVertex( "2" );

        UndirectedMutableGraph<BaseLabeledVertex, BaseLabeledEdge> g =
            newUndirectedMutableGraph( new AbstractGraphConnection<BaseLabeledVertex, BaseLabeledEdge>()
            {

                @Override
                public void connect()
                {
                    BaseLabeledVertex one = addVertex( new BaseLabeledVertex( "1" ) );
                    addVertex( two );
                    BaseLabeledVertex three = addVertex( new BaseLabeledVertex( "3" ) );

                    addEdge( new BaseLabeledEdge( "1 -> 2" ) ).from( one ).to( two );
                    addEdge( new BaseLabeledEdge( "2 -> 3" ) ).from( two ).to( three );
                    addEdge( new BaseLabeledEdge( "3 -> 1" ) ).from( three ).to( one );
                    }

            } );
        coloring( g ).withColors( createColorsList( 1 ) ).applyingBackTrackingAlgorithm();
    }

    @Test
    public void testCromaticNumber()
    {
        final BaseLabeledVertex two = new BaseLabeledVertex( "2" );

        UndirectedMutableGraph<BaseLabeledVertex, BaseLabeledEdge> g =
            newUndirectedMutableGraph( new AbstractGraphConnection<BaseLabeledVertex, BaseLabeledEdge>()
            {

                @Override
                public void connect()
                {
                    BaseLabeledVertex one = addVertex( new BaseLabeledVertex( "1" ) );
                    addVertex( two );
                    BaseLabeledVertex three = addVertex( new BaseLabeledVertex( "3" ) );

                    addEdge( new BaseLabeledEdge( "1 -> 2" ) ).from( one ).to( two );
                    addEdge( new BaseLabeledEdge( "2 -> 3" ) ).from( two ).to( three );
                    addEdge( new BaseLabeledEdge( "3 -> 1" ) ).from( three ).to( one );
                }

            } );

        ColoredVertices<BaseLabeledVertex, Integer> coloredVertex = new ColoredVertices<BaseLabeledVertex, Integer>();
        coloredVertex.addColor( two, 2 );

        ColoredVertices<BaseLabeledVertex, Integer> coloredVertices =
            coloring( g ).withColors( createColorsList( 3 ) ).applyingBackTrackingAlgorithm( coloredVertex );
        Assert.assertNotNull( coloredVertices );
        Assert.assertEquals( 3, coloredVertices.getRequiredColors() );
        Assert.assertEquals( new Integer( 2 ), coloredVertices.getColor( two ) );
        checkColoring( g, coloredVertices );
    }

    @Test
    public void testCromaticNumberComplete()
    {
        UndirectedMutableGraph<BaseLabeledVertex, BaseLabeledEdge> g1 =
            new UndirectedMutableGraph<BaseLabeledVertex, BaseLabeledEdge>();

        buildCompleteGraph( 100, g1 );

        ColoredVertices<BaseLabeledVertex, Integer> coloredVertices =
            coloring( g1 ).withColors( createColorsList( 100 ) ).applyingBackTrackingAlgorithm();
        Assert.assertNotNull( coloredVertices );
        Assert.assertEquals( 100, coloredVertices.getRequiredColors() );
        checkColoring( g1, coloredVertices );
    }

    @Test
    public void testCromaticNumberBiparted()
    {
        UndirectedMutableGraph<BaseLabeledVertex, BaseLabeledEdge> g1 =
            new UndirectedMutableGraph<BaseLabeledVertex, BaseLabeledEdge>();

        buildBipartedGraph( 100, g1 );

        ColoredVertices<BaseLabeledVertex, Integer> coloredVertices =
            coloring( g1 ).withColors( createColorsList( 2 ) ).applyingBackTrackingAlgorithm();
        Assert.assertNotNull( coloredVertices );
        Assert.assertEquals( 2, coloredVertices.getRequiredColors() );
        checkColoring( g1, coloredVertices );
    }

    @Test
    public void testCromaticNumberSparseGraph()
    {
        UndirectedMutableGraph<BaseLabeledVertex, BaseLabeledEdge> g1 =
            new UndirectedMutableGraph<BaseLabeledVertex, BaseLabeledEdge>();
        for ( int i = 0; i < 100; i++ )
        {
            g1.addVertex( new BaseLabeledVertex( String.valueOf( i ) ) );
        }

        ColoredVertices<BaseLabeledVertex, Integer> coloredVertices =
            coloring( g1 ).withColors( createColorsList( 1 ) ).applyingBackTrackingAlgorithm();
        Assert.assertNotNull( coloredVertices );
        Assert.assertEquals( 1, coloredVertices.getRequiredColors() );
        checkColoring( g1, coloredVertices );
    }

    /**
     * see <a href="http://en.wikipedia.org/wiki/Crown_graph">wiki</a> for more details
     */
    @Test
    public void testCrawnGraph()
    {
        UndirectedMutableGraph<BaseLabeledVertex, BaseLabeledEdge> g =
            new UndirectedMutableGraph<BaseLabeledVertex, BaseLabeledEdge>();

        buildCrownGraph( 6, g );

        ColoredVertices<BaseLabeledVertex, Integer> coloredVertices =
            coloring( g ).withColors( createColorsList( 2 ) ).applyingBackTrackingAlgorithm();
        Assert.assertNotNull( coloredVertices );
        Assert.assertEquals( 2, coloredVertices.getRequiredColors() );
        checkColoring( g, coloredVertices );
    }

    @Test
    public void testSudoku()
        throws Exception
    {
        UndirectedMutableGraph<BaseLabeledVertex, BaseLabeledEdge> g1 =
            new UndirectedMutableGraph<BaseLabeledVertex, BaseLabeledEdge>();
        BaseLabeledVertex[][] grid = buildSudokuGraph( g1 );

        ColoredVertices<BaseLabeledVertex, Integer> sudoku =
            coloring( g1 ).withColors( createColorsList( 9 ) ).applyingBackTrackingAlgorithm();
        Assert.assertNotNull( sudoku );
        checkColoring( g1, sudoku );
        Assert.assertEquals( 9, sudoku.getRequiredColors() );

        // Printout the result
        StringBuilder sb = new StringBuilder();
        NumberFormat nf = new DecimalFormat( "00" );
        sb.append( "\n" );
        for ( int i = 0; i < 9; i++ )
        {
            for ( int j = 0; j < 9; j++ )
            {
                sb.append( "| " + nf.format( sudoku.getColor( grid[i][j] ) ) + " | " );
            }
            sb.append( "\n" );
        }
        Logger.getAnonymousLogger().fine( sb.toString() );
    }

    @Test
    public void testSudokuWithConstraints()
        throws Exception
    {
        UndirectedMutableGraph<BaseLabeledVertex, BaseLabeledEdge> g1 =
            new UndirectedMutableGraph<BaseLabeledVertex, BaseLabeledEdge>();
        BaseLabeledVertex[][] grid = buildSudokuGraph( g1 );

        ColoredVertices<BaseLabeledVertex, Integer> predefinedColor = new ColoredVertices<BaseLabeledVertex, Integer>();
        predefinedColor.addColor( grid[0][0], 1 );
        predefinedColor.addColor( grid[5][5], 8 );
        predefinedColor.addColor( grid[1][2], 5 );

        ColoredVertices<BaseLabeledVertex, Integer> sudoku =
            coloring( g1 ).withColors( createColorsList( 9 ) ).applyingBackTrackingAlgorithm( predefinedColor );
        Assert.assertNotNull( sudoku );
        checkColoring( g1, sudoku );
        Assert.assertEquals( 9, sudoku.getRequiredColors() );

        Assert.assertEquals( new Integer( 1 ), sudoku.getColor( grid[0][0] ) );
        Assert.assertEquals( new Integer( 8 ), sudoku.getColor( grid[5][5] ) );
        Assert.assertEquals( new Integer( 5 ), sudoku.getColor( grid[1][2] ) );

        // Printout the result
        StringBuilder sb = new StringBuilder();
        NumberFormat nf = new DecimalFormat( "00" );
        sb.append( "\n" );
        for ( int i = 0; i < 9; i++ )
        {
            for ( int j = 0; j < 9; j++ )
            {
                sb.append( "| " );
                sb.append( nf.format( sudoku.getColor( grid[i][j] ) ) );
                sb.append( " | " );
            }
            sb.append( "\n" );
        }
        Logger.getAnonymousLogger().fine( sb.toString() );

    }

}
