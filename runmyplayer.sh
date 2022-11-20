# !/bin/bash
cd "$(dirname "$0")"
if [[ "${1^^}" == "WHITE" ]]
then
    ant BlutForceWhite -Dargs="${1^^} ${2} ${3}"
elif  [[ "${1^^}" == "BLACK" ]]
then
    ant BlutForceBlack -Dargs="${1^^} ${2} ${3}"
else
    echo "Error: player not defined"
fi
