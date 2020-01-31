package ru.javaops.masterjava.xml.util;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.io.Resources;
import j2html.tags.ContainerTag;
import one.util.streamex.StreamEx;
import ru.javaops.masterjava.xml.schema.ObjectFactory;
import ru.javaops.masterjava.xml.schema.Payload;
import ru.javaops.masterjava.xml.schema.Project;
import ru.javaops.masterjava.xml.schema.User;

import javax.xml.stream.events.XMLEvent;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import static j2html.TagCreator.*;

public class MainXml {
    private static final Comparator<User> USER_COMPARATOR = Comparator.comparing(User::getValue).thenComparing(User::getEmail);
    private static final String PROJECT = "Project";
    private static final String GROUP = "Group";
    private static final String USERS = "Users";
    private static final String PROJECTS = "Projects";

    public static void main(String[] args) throws Exception {
        System.out.format("Hello MasterJava!");
        System.out.println();

        URL payloadURL = Resources.getResource(args[0]);
        String project = args[1];

        Set<User> users = parseXmlFileWithJaxb(project, payloadURL);
        final String s = outHtml(users, project, Paths.get("users.html"));
        System.out.println("users by jaxb:");
        System.out.println(s);

        Set<User> usersByStax = parseXmlFileWithStax(project, payloadURL);
        System.out.println("users by stax:");
        usersByStax.forEach(u -> System.out.println("Name " + u.getValue() + ", email " + u.getEmail()));
    }

    private static Set<User> parseXmlFileWithStax(String projectName, URL payloadURL) throws Exception {
        try (InputStream is = payloadURL.openStream()) {
            StaxStreamProcessor processor = new StaxStreamProcessor(is);
            Set<String> groupNames = new HashSet<>();
            Set<User> users = new TreeSet<>(USER_COMPARATOR);
            String element;

            //Projects loop
            while (processor.startElement(PROJECT, PROJECTS)) {
                if (projectName.equals(processor.getAttribute("name"))) {
                    while (processor.startElement(GROUP, PROJECT)) {
                        groupNames.add(processor.getAttribute("name"));
                    }
                    break;
                }
            }
            if (groupNames.isEmpty()) {
                throw new IllegalArgumentException("Invalid project name = " + projectName + "or no groups found");
            }

            //Users loop
            JaxbParser jaxbParser = new JaxbParser(User.class);
            while (processor.doUntil(XMLEvent.START_ELEMENT, "User")) {
                String groupRefs = processor.getAttribute("groupRefs");
                if (!Collections.disjoint(groupNames, Splitter.on(' ').splitToList(Strings.nullToEmpty(groupRefs)))) {
                    User user = jaxbParser.unmarshal(processor.getReader(), User.class);
                    users.add(user);
                }
            }
            return users;
        }
    }

    private static String outHtml(Set<User> users, String project, Path path) throws IOException {
        try (Writer writer = Files.newBufferedWriter(path)) {
            ContainerTag table = table().with(tr().with(th("FullName"), th("email")));
            users.forEach(u -> table.with(tr().with(td(u.getValue()), td(u.getEmail()))));
            table.attr("border", "1");
            table.attr("cellpadding", "8");
            table.attr("cellspacing", "0");

            String out = html().with(
                    head().with(title(project + " users")),
                    body().with(h1(project + " users"), table)
            ).render();

            writer.write(out);
            return out;
        }
    }

    private static Set<User> parseXmlFileWithJaxb(String projectName, URL payloadURL) throws Exception {
        JaxbParser JAXB_PARSER = new JaxbParser(ObjectFactory.class);
        JAXB_PARSER.setSchema(Schemas.ofClasspath("payload.xsd"));

        try (InputStream is = payloadURL.openStream()) {
            Payload payload = JAXB_PARSER.unmarshal(is);
            Project project = payload.getProjects().getProject().stream()
                    .filter(pr -> pr.getName().equals(projectName))
                    .findAny()
                    .orElseThrow(() -> new IllegalArgumentException("Invalid project name"));

            Set<Project.Group> groups = new HashSet<>(project.getGroup());

            List<User> users = payload.getUsers().getUser();

            return users.stream()
                    .filter(u -> StreamEx.of(u.getGroupRefs().stream())
                            .findAny(groups::contains)
                            .isPresent())
                    .collect(Collectors.toCollection(() -> new TreeSet<>(USER_COMPARATOR)));
        }
    }
}
