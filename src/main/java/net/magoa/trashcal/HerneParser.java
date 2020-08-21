package net.magoa.trashcal;

import net.fortuna.ical4j.data.CalendarOutputter;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Date;
import net.fortuna.ical4j.model.TimeZone;
import net.fortuna.ical4j.model.TimeZoneRegistryFactory;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.CalScale;
import net.fortuna.ical4j.model.property.Description;
import net.fortuna.ical4j.model.property.ProdId;
import net.fortuna.ical4j.model.property.Version;
import net.fortuna.ical4j.util.RandomUidGenerator;
import net.fortuna.ical4j.util.UidGenerator;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HerneParser {

    private static final DateTimeFormatter MONTH_GERMAN = DateTimeFormatter.ofPattern("MMMM", Locale.GERMAN);
    private static final UidGenerator UID_GENERATOR = new RandomUidGenerator();
    private static final TimeZone TIMEZONE = TimeZoneRegistryFactory.getInstance()
            .createRegistry()
            .getTimeZone(ZoneId.systemDefault().getId());

    public static void main(String[] args) {
        try {
            var entries = parse(args[0]);
            write(entries, args[1]);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static List<TrashEntry> parse(String path) throws IOException {
        var result = new ArrayList<TrashEntry>();

        var document = Jsoup.parse(new File(path), "UTF-8");
        var headerElements = document.getElementsByClass("headerDif");
        var headerText = headerElements.get(0).text();
        var year = Integer.parseInt(headerText.substring(15));

        var monthElements = document.getElementsByClass("monat_table");
        for (var monthElement : monthElements) {
            var monthName = monthElement.getElementsByClass("kw_td_ueberschrift").text();
            var month = MONTH_GERMAN.parse(monthName).get(ChronoField.MONTH_OF_YEAR);
            var cells = monthElement.getElementsByClass("kw_td");

            int dayOfMonth = -1;
            for (Element cell : cells) {
                if (cell.hasClass("kw_td_tag_sonntag") || cell.hasClass("kw_td_tag")) {
                    dayOfMonth = Integer.parseInt(cell.text(), 10);
                }

                var images = cell.getElementsByTag("img");
                if (!images.isEmpty()) {
                    var trashType = TrashType.findByValue(images.attr("alt"));
                    result.add(new TrashEntry(LocalDate.of(year, month, dayOfMonth), trashType));
                }
            }
        }
        return result;
    }

    private static void write(List<TrashEntry> entries, String path) throws IOException {
        var ical = new Calendar();
        ical.getProperties().add(new ProdId("-//Abfallkalender//Herne"));
        ical.getProperties().add(Version.VERSION_2_0);
        ical.getProperties().add(CalScale.GREGORIAN);
        ical.getProperties().add(TIMEZONE.getVTimeZone().getTimeZoneId());

        for (TrashEntry entry : entries) {
            var date = new Date(java.util.Date.from(entry.getDate().atStartOfDay(ZoneId.of("UTC")).toInstant()));
            var event = new VEvent(date, date, entry.getType().getDescription());
            event.getProperties().add(new Description(entry.getType().getDescription()));
            event.getProperties().add(UID_GENERATOR.generateUid());
            event.getProperties().add(TIMEZONE.getVTimeZone().getTimeZoneId());
            ical.getComponents().add(event);
        }

        try (FileOutputStream outputStream = new FileOutputStream(path)) {
            new CalendarOutputter().output(ical, outputStream);
        }
    }
}
