import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

String basicAuthToken = ''

def tagToId = [
        'M': '9b3f3139-beef-4cf0-aa28-a4264c2804de',
        'S': '6816f216-eb07-4c9a-958c-46fec4c49aad',
        'A': 'cce01f73-67a5-46dc-982e-3e7ab1da1865',
        'R': 'afee734e-ecb5-4758-8fd4-e389a7b23111',
        'P': '93b2378e-a3d0-411e-8936-e84d4ac0c6cd',
        'D': 'd10dc389-1181-412c-95d6-61e9e7ce5b18'
]

def file = new File('actracker.csv')
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