import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

// START INPUT PARAMS
String basicAuthToken = ''
def tagToId = [
        'tagName1': 'tagId1',
        'tagName2': 'tagId2',
        'tagName3': 'tagId3',
]
// END INPUT PARAMS

def file = new File('actracker_import.csv')
def rows = file.readLines()

rows.each { row ->
    def cells = row.split(',')
    def tag = tagToId.get(cells[0])
    def startTime = LocalDateTime.parse(cells[1], DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")).atZone(ZoneId.of("CET")).toInstant()
    def endTime = null
    def comment = null
    if (cells.size() > 2) {
        endTime = LocalDateTime.parse(cells[2], DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")).atZone(ZoneId.of("CET")).toInstant()
    }
    if (cells.size() > 3) {
        comment = cells[3].replace('"', '')
    }

    String activity = """
        {
            "title": ${comment ? "\"${comment}\"" : 'null'},
            "startTimestamp": ${startTime.toEpochMilli()},
            "endTimestamp": ${endTime ? endTime.toEpochMilli() : 'null'},
            "tags": [\"${tag}\"]   
        }
    """

    def post = new URL("https://prod.cloud.equino.ovh/actracker-api/activity").openConnection();
    def message = activity
    post.setRequestMethod("POST")
    post.setDoOutput(true)
    post.setRequestProperty("Content-Type", "application/json")
    post.setRequestProperty("authorization", basicAuthToken)
    post.getOutputStream().write(message.getBytes("UTF-8"));
    def postRC = post.getResponseCode();
    if (!postRC.equals(200)) {
        throw new IllegalArgumentException("Error $postRC occcurred for activity: $activity")
    }
    println "Processed request with startDate=$startTime"

}