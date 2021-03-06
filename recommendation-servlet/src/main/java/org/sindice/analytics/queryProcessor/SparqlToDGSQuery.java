/*******************************************************************************
 * Copyright (c) 2012 National University of Ireland, Galway. All Rights Reserved.
 *
 *
 * This project is a free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 *
 * This project is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public
 * License along with this project. If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
/**
 * @project sparql-editor-servlet
 * @author Campinas Stephane [ 19 Mar 2012 ]
 * @link stephane.campinas@deri.org
 */
package org.sindice.analytics.queryProcessor;

import java.util.List;

import org.openrdf.query.MalformedQueryException;
import org.openrdf.sindice.query.parser.sparql.ast.ASTQueryContainer;
import org.openrdf.sindice.query.parser.sparql.ast.VisitorException;
import org.sindice.analytics.queryProcessor.QueryProcessor.POFMetadata;
import org.sindice.analytics.queryProcessor.QueryProcessor.RecommendationType;

/**
 * 
 */
public final class SparqlToDGSQuery {

  private static RecommendationType type;

  private SparqlToDGSQuery() {
  }

  public static POFMetadata process(ASTQueryContainer ast, List<String> varsToProject)
  throws MalformedQueryException, VisitorException {
    if (!ast.containsQuery()) {
      return null;
    }

    ASTVarGenerator.reset();
    // Retrieve the POF metadata
    final POFMetadata meta = PofNodesMetadata.retrieve(ast);
    // expand each TP into simple one: denormalize syntax sugar constructions
    DeNormalizeAST.process(ast);

    // Ensure the query is valid for recommendation
    ValidateQ4Recommendations.process(ast);

    ASTVarGenerator.addVars(ASTVarProcessor.process(ast));
    /*
     * Map SPARQL query to a Data Graph Summary query
     */
    // 1. Materialize the POF
    type = PointOfFocusProcessor.process(ast, varsToProject);
    // 2. Remove Content Elements
    ContentRemovalProcessor.process(ast);
    // Define Recommendation Scope
    RecommendationScopeProcessor.process(ast);
    // TODO: Optimize the query by removing unnecessary parts, e.g., optional, unions
    // 3. Map to the DGS query
    SparqlTranslationProcessor.process(ast);
    return meta;
  }

  /**
   * @return the type
   */
  public static RecommendationType getRecommendationType() {
    return type;
  }

}
