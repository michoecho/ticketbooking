#!/bin/bash
exec 2>&1
(set -x; curl -sS http://localhost:8080/screenings) | ./jq
(set -x; curl -sS http://localhost:8080/screenings/161) | ./jq

for testname in {\
first_reservation,\
bad_firstname1,bad_firstname2,bad_firstname3,\
bad_surname1,bad_surname2,\
empty_reservation,nonexistent_seat,duplicate_seat,\
nonexistent_ticket_type,single_seat_left_over,\
second_reservation}
do
	filename=tests/"$testname".json
	printf "\n\n"
	(set -x; cat "$filename") | ./jq
	(set -x; curl -sS -X POST -d @"$filename" --header "Content-Type:application/json" \
		http://localhost:8080/screenings/161/reservations) | ./jq
done
