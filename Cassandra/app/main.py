# http://datastax.github.io/python-driver/getting_started.html

from cassandra.cluster import Cluster
from cassandra import ReadTimeout

import logging as log

cluster = Cluster(['159.65.140.125', '167.99.67.66', '206.189.40.171', '206.189.47.228'])
session = cluster.connect('wijayaerick')

rows = session.execute('SELECT user_id, fname, lname FROM users')
for (user_id, fname, lname) in rows:
    print(user_id, fname, lname)

session.execute(
    """
    INSERT INTO users (user_id, fname, lname)
    VALUES (%s, %s, %s)
    """,
    (1234, "dog", "smith")
)

query = "SELECT * FROM users WHERE lname=%s ALLOW FILTERING"
future = session.execute_async(query, ["smith"])
try:
    rows = future.result()
    for (user_id, fname, lname) in rows:
        print(user_id, fname, lname)
except ReadTimeout:
    log.exception("Query timed out:")
