/*

Copyright 2010, Google Inc.
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are
met:

    * Redistributions of source code must retain the above copyright
notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above
copyright notice, this list of conditions and the following disclaimer
in the documentation and/or other materials provided with the
distribution.
    * Neither the name of Google Inc. nor the names of its
contributors may be used to endorse or promote products derived from
this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
"AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,           
DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY           
THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

*/

package com.google.refine.commands.project;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;

import com.google.refine.ProjectManager;

import static com.google.refine.io.FileProjectManager.REQUEST_ATTEIBUTE_DATASET;

import com.google.refine.ProjectMetadata;
import com.google.refine.browsing.Engine;
import com.google.refine.browsing.FilteredRows;
import com.google.refine.browsing.RowVisitor;
import com.google.refine.commands.Command;
import com.google.refine.exporters.CsvExporter;
import com.google.refine.exporters.CustomizableTabularExporterUtilities;
import com.google.refine.exporters.Exporter;
import com.google.refine.exporters.ExporterRegistry;
import com.google.refine.exporters.StreamExporter;
import com.google.refine.exporters.TabularSerializer;
import com.google.refine.exporters.WriterExporter;
import com.google.refine.exporters.TabularSerializer.CellData;
import com.google.refine.model.Column;
import com.google.refine.model.Project;
import com.google.refine.model.Row;
import com.google.refine.util.JSONUtilities;

import edu.dfci.cccb.mev.dataset.domain.contract.Dataset;
import edu.dfci.cccb.mev.dataset.domain.contract.Dimension;
import edu.dfci.cccb.mev.dataset.domain.contract.Dimension.Type;
import edu.dfci.cccb.mev.dataset.domain.contract.Selection;
import edu.dfci.cccb.mev.dataset.domain.contract.Selections;

public class ExportSetCommand extends Command {

    @SuppressWarnings("unchecked")
    static public Properties getRequestParameters(HttpServletRequest request) {
        Properties options = new Properties();

        Enumeration<String> en = request.getParameterNames();
        while (en.hasMoreElements()) {
            String name = en.nextElement();
            options.put(name, request.getParameter(name));
        }
        return options;
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        logger.info (String.format("******************* EXPORT SET: %s *******************", request.getParameter ("set-name")));
        ProjectManager.getSingleton().setBusy(true);
        try {
            
          /*
          try {
              String name = request.getParameter("name");
              ProjectMetadata pm = getProjectMetadata(request);
              
              pm.setName(name);
              
              respond(response, "{ \"code\" : \"ok\" }");
          } catch (Exception e) {
              respondException(response, e);
          }
           */
          
            Project project = getProject(request);            
            
            Engine engine = getEngine(request, project);                        
            final Dataset heatmap = (Dataset)request.getAttribute (REQUEST_ATTEIBUTE_DATASET);
            final Selections selections = heatmap.dimension (Dimension.Type.COLUMN).selections ();
            final String setName = request.getParameter ("set-name");
            final Properties properties = new Properties ();            
            properties.put ("set-description", request.getParameter ("set-description"));
            properties.put ("set-color", request.getParameter ("set-color"));
            final List<String> keys = new ArrayList<String> ();
            
            RowVisitor visitor = new RowVisitor() {
                int rowCount = 0;
                Column theIdColumn;
                @Override
                public void start(Project project) {
                              
                  //if no id column found, assume first column is the id
                  List<Column> columns = project.columnModel.columns;
                  theIdColumn=columns.get (0);
                  
                  for (Column column : columns) {
                      String name = column.getName();
                      if(name.equalsIgnoreCase ("annotationId") || name.equalsIgnoreCase ("id")){
                        theIdColumn=column;
                        break;
                      }
                  }
                }

                @Override
                public boolean visit(Project project, int rowIndex, Row row) {                    
                    String cellData = row.getCell(theIdColumn.getCellIndex()).value.toString ();
                    if (cellData != null) {
                      keys.add (cellData);
                      rowCount++;
                    }
                    return false;
                }

                @Override
                public void end(Project project) {                    
                  selections.put (setName, properties, keys);
                }
            };

            FilteredRows filteredRows = engine.getAllFilteredRows();
            filteredRows.accept(project, visitor);
            ProjectManager.getSingleton().save(true);
            respond(response, "{ \"code\" : \"ok\" }");
            
        } catch (Exception e) {
            // Use generic error handling rather than our JSON handling
            //throw new ServletException(e);
            respondException(response, e);
        } finally {
            ProjectManager.getSingleton().setBusy(false);            
        }
    }
}