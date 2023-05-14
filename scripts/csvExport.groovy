import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

// START INPUT PARAMS
String basicAuthToken = ''
ZoneId zoneId = ZoneId.of("Europe/Warsaw")
def tagIdToName = [
        'tagId1': 'tagName1',
        'tagId2': 'tagName2',
        'tagId3': 'tagName3',
]
// END INPUT PARAMS

def dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
def jsonSlurper = new groovy.json.JsonSlurper()

def file = new File('actracker_export.csv')
file.delete()
file.createNewFile()

def pageCount = 0
def pageId = ''

do {
    def get = new URL("https://prod.cloud.equino.ovh/actracker-api/activity/matching?pageId=${pageId}&pageSize=50").openConnection()
    get.setRequestMethod("GET")
    get.setRequestProperty("Content-Type", "application/json")
    get.setRequestProperty("authorization", basicAuthToken)
    assert get.getResponseCode() == 200
    BufferedReader br = new BufferedReader(new InputStreamReader(get.getInputStream()))
    StringBuilder sb = new StringBuilder()
    String line
    while ((line = br.readLine()) != null) {
        sb.append(line + "\n")
    }
    br.close()

    def activityResult = jsonSlurper.parseText(sb.toString())

    pageId = activityResult.nextPageId

    activityResult.results.each { activity ->
        activity.tags.each { tag ->
            if (tagIdToName.containsKey(tag)) {

                String category = tagIdToName.get(tag)
                LocalDateTime startTime = activity.startTimestamp != null
                        ? LocalDateTime.ofInstant(Instant.ofEpochMilli(activity.startTimestamp), zoneId)
                        : null
                LocalDateTime endTime = activity.endTimestamp != null
                        ? LocalDateTime.ofInstant(Instant.ofEpochMilli(activity.endTimestamp), zoneId)
                        : null
                String comment = activity.title != null
                        ? activity.title
                        : ''

                file.append("${category},${startTime.format(dateTimeFormatter)},${endTime.format(dateTimeFormatter)},${comment}\n")
            }
        }
    }

    println "Page number ${pageCount++} processed"
    println "Next page ID: $pageId"

} while (pageId != null)