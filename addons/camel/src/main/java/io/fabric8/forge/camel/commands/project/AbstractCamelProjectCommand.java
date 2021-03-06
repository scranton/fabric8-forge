/**
 *  Copyright 2005-2015 Red Hat, Inc.
 *
 *  Red Hat licenses this file to you under the Apache License, version
 *  2.0 (the "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 *  implied.  See the License for the specific language governing
 *  permissions and limitations under the License.
 */
package io.fabric8.forge.camel.commands.project;

import java.io.PrintStream;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import javax.inject.Inject;

import io.fabric8.forge.addon.utils.CamelProjectHelper;
import io.fabric8.forge.addon.utils.XmlLineNumberParser;
import io.fabric8.forge.camel.commands.project.completer.RouteBuilderEndpointsCompleter;
import io.fabric8.forge.camel.commands.project.completer.XmlEndpointsCompleter;
import io.fabric8.forge.camel.commands.project.completer.XmlFileCompleter;
import io.fabric8.forge.camel.commands.project.converter.NodeDtoConverter;
import io.fabric8.forge.camel.commands.project.converter.NodeDtoLabelConverter;
import io.fabric8.forge.camel.commands.project.dto.ComponentDto;
import io.fabric8.forge.camel.commands.project.dto.ContextDto;
import io.fabric8.forge.camel.commands.project.dto.NodeDto;
import io.fabric8.forge.camel.commands.project.dto.NodeDtos;
import io.fabric8.forge.camel.commands.project.helper.CamelCommandsHelper;
import io.fabric8.forge.camel.commands.project.helper.CamelXmlHelper;
import io.fabric8.utils.Strings;
import org.apache.camel.catalog.CamelCatalog;
import org.jboss.forge.addon.convert.Converter;
import org.jboss.forge.addon.convert.ConverterFactory;
import org.jboss.forge.addon.dependencies.Coordinate;
import org.jboss.forge.addon.dependencies.Dependency;
import org.jboss.forge.addon.dependencies.builder.CoordinateBuilder;
import org.jboss.forge.addon.parser.java.facets.JavaSourceFacet;
import org.jboss.forge.addon.projects.Project;
import org.jboss.forge.addon.projects.ProjectFactory;
import org.jboss.forge.addon.projects.Projects;
import org.jboss.forge.addon.projects.facets.ResourcesFacet;
import org.jboss.forge.addon.projects.facets.WebResourcesFacet;
import org.jboss.forge.addon.projects.ui.AbstractProjectCommand;
import org.jboss.forge.addon.resource.FileResource;
import org.jboss.forge.addon.ui.context.UIContext;
import org.jboss.forge.addon.ui.context.UIExecutionContext;
import org.jboss.forge.addon.ui.input.UISelectOne;
import org.jboss.forge.addon.ui.input.ValueChangeListener;
import org.jboss.forge.addon.ui.input.events.ValueChangeEvent;
import org.jboss.forge.addon.ui.metadata.UICommandMetadata;
import org.jboss.forge.addon.ui.util.Categories;
import org.jboss.forge.addon.ui.util.Metadata;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import static io.fabric8.forge.camel.commands.project.helper.CamelCatalogHelper.createComponentDto;
import static io.fabric8.forge.camel.commands.project.helper.CollectionHelper.first;

public abstract class AbstractCamelProjectCommand extends AbstractProjectCommand {

    public static String CATEGORY = "Camel";

    @Inject
    protected ProjectFactory projectFactory;

    @Inject
    protected ConverterFactory converterFactory;

    @Inject
    private CamelCatalog camelCatalog;

    protected static void configureNode(final UIContext context, final Project project, final String first, final UISelectOne<String> xml, UISelectOne<NodeDto> node) {
        node.setValueConverter(new NodeDtoConverter(project, context, xml));
        node.setItemLabelConverter(new NodeDtoLabelConverter());
        node.setValueChoices(new Callable<Iterable<NodeDto>>() {
            @Override
            public Iterable<NodeDto> call() throws Exception {
                String xmlResourceName = xml.getValue();
                if (Strings.isNullOrBlank(xmlResourceName)) {
                    xmlResourceName = first;
                }
                List<ContextDto> camelContexts = CamelXmlHelper.loadCamelContext(context, project, xmlResourceName);
                return NodeDtos.toNodeList(camelContexts);
            }
        });
    }

    @Override
    protected boolean isProjectRequired() {
        return true;
    }

    @Override
    public UICommandMetadata getMetadata(UIContext context) {
        return Metadata.forCommand(CamelDeleteNodeXmlCommand.class).name(
                "Camel: Delete Node XML").category(Categories.create(CATEGORY))
                .description("Deletes a node from a Camel XML file");
    }

    @Override
    public boolean isEnabled(UIContext context) {
        boolean enabled = super.isEnabled(context);
        if (!enabled) {
            return false;
        }
        if (requiresCamelSetup()) {
            // requires camel is already setup
            Project project = getSelectedProjectOrNull(context);
            if (project != null) {
                return findCamelCoreDependency(project) != null;
            }
        }
        return false;
    }

    protected Project getSelectedProjectOrNull(UIContext context) {
        return Projects.getSelectedProject(this.getProjectFactory(), context);
    }

    protected boolean requiresCamelSetup() {
        return true;
    }

    @Override
    protected ProjectFactory getProjectFactory() {
        return projectFactory;
    }

    protected ConverterFactory getConverterFactory() {
        return converterFactory;
    }

    protected CamelCatalog getCamelCatalog() {
        return camelCatalog;
    }

    protected PrintStream getOutput(UIExecutionContext context) {
        return context.getUIContext().getProvider().getOutput().out();
    }

    protected Dependency findCamelCoreDependency(Project project) {
        return CamelProjectHelper.findCamelCoreDependency(project);
    }

    protected Set<Dependency> findCamelArtifacts(Project project) {
        return CamelProjectHelper.findCamelArtifacts(project);
    }

    protected Coordinate createCoordinate(String groupId, String artifactId, String version) {
        CoordinateBuilder builder = CoordinateBuilder.create()
                .setGroupId(groupId)
                .setArtifactId(artifactId);
        if (version != null) {
            builder = builder.setVersion(version);
        }

        return builder;
    }

    protected Coordinate createCamelCoordinate(String artifactId, String version) {
        return createCoordinate("org.apache.camel", artifactId, version);
    }

    protected RouteBuilderEndpointsCompleter createRouteBuilderEndpointsCompleter(UIContext context) {
        Project project = getSelectedProject(context);
        return createRouteBuilderEndpointsCompleter(project);
    }

    protected RouteBuilderEndpointsCompleter createRouteBuilderEndpointsCompleter(Project project) {
        JavaSourceFacet facet = project.getFacet(JavaSourceFacet.class);
        return new RouteBuilderEndpointsCompleter(facet);
    }

    protected XmlEndpointsCompleter createXmlEndpointsCompleter(UIContext context) {
        Project project = getSelectedProject(context);
        return createXmlEndpointsCompleter(project);
    }

    protected XmlEndpointsCompleter createXmlEndpointsCompleter(Project project) {
        final ResourcesFacet resourcesFacet = project.getFacet(ResourcesFacet.class);
        WebResourcesFacet webResourcesFacet = null;
        if (project.hasFacet(WebResourcesFacet.class)) {
            webResourcesFacet = project.getFacet(WebResourcesFacet.class);
        }
        return new XmlEndpointsCompleter(resourcesFacet, webResourcesFacet);
    }

    protected XmlFileCompleter createXmlFileCompleter(Project project) {
        final ResourcesFacet resourcesFacet = project.getFacet(ResourcesFacet.class);
        WebResourcesFacet webResourcesFacet = null;
        if (project.hasFacet(WebResourcesFacet.class)) {
            webResourcesFacet = project.getFacet(WebResourcesFacet.class);
        }
        return new XmlFileCompleter(resourcesFacet, webResourcesFacet);
    }

    protected XmlFileCompleter createXmlFileCompleter(UIContext context) {
        Project project = getSelectedProject(context);
        return createXmlFileCompleter(project);
    }

    protected FileResource getXmlResourceFile(Project project, String xmlResourceName) {
        ResourcesFacet facet = project.getFacet(ResourcesFacet.class);
        WebResourcesFacet webResourcesFacet = null;
        if (project.hasFacet(WebResourcesFacet.class)) {
            webResourcesFacet = project.getFacet(WebResourcesFacet.class);
        }
        FileResource file = facet != null ? facet.getResource(xmlResourceName) : null;
        if (file == null || !file.exists()) {
            file = webResourcesFacet != null ? webResourcesFacet.getWebResource(xmlResourceName) : null;
        }
        return file;
    }

    protected String configureXml(Project project, UISelectOne<String> xml) {
        XmlFileCompleter xmlFileCompleter = createXmlFileCompleter(project);
        Set<String> files = xmlFileCompleter.getFiles();

        // use value choices instead of completer as that works better in web console
        final String first = first(files);
        xml.setValueChoices(files);
        if (files.size() == 1) {
            // lets default the value if there's only one choice
            xml.setDefaultValue(first);
        }
        return first;
    }

    protected void configureComponentName(Project project, final UISelectOne<ComponentDto> componentName, boolean consumerOnly, boolean producerOnly) {
        componentName.setValueChoices(CamelCommandsHelper.createComponentDtoValues(project, getCamelCatalog(), null, false, consumerOnly, producerOnly));
        // include converter from string->dto
        componentName.setValueConverter(new Converter<String, ComponentDto>() {
            @Override
            public ComponentDto convert(String text) {
                return createComponentDto(getCamelCatalog(), text);
            }
        });
        componentName.setValueConverter(new Converter<String, ComponentDto>() {
            @Override
            public ComponentDto convert(String name) {
                return createComponentDto(getCamelCatalog(), name);
            }
        });
        // show note about the chosen component
        componentName.addValueChangeListener(new ValueChangeListener() {
            @Override
            public void valueChanged(ValueChangeEvent event) {
                ComponentDto component = (ComponentDto) event.getNewValue();
                if (component != null) {
                    String description = component.getDescription();
                    componentName.setNote(description != null ? description : "");
                } else {
                    componentName.setNote("");
                }
            }
        });
    }

    protected Element getSelectedCamelElementNode(Project project, String xmlResourceName, String key) throws Exception {
        FileResource file = getXmlResourceFile(project, xmlResourceName);
        Document root = XmlLineNumberParser.parseXml(file.getResourceInputStream(), "camelContext,routes,rests", "http://camel.apache.org/schema/spring");
        Element selectedElement = null;
        if (root != null) {
            Node selectedNode = CamelXmlHelper.findCamelNodeInDocument(root, key);
            if (selectedNode instanceof Element) {
                selectedElement = (Element) selectedNode;
            }
        }
        return selectedElement;
    }
}
