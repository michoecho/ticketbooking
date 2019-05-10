#!/bin/bash
exec 2>&1
(set -x; curl -sS http://localhost:8080/screenings) | ./jq
(set -x; curl -sS http://localhost:8080/screenings/161) | ./jq
(set -x; cat tests/first_reservation.json) | ./jq
(set -x; curl -sS -X POST -d @tests/first_reservation.json --header "Content-Type:application/json" \
	http://localhost:8080/screenings/161/reservations) | ./jq
