import os
import json

fullpath = r"scripts\testfile.json"
	
jsonfile = list()
sub_jsonfile = dict()
prop = list()
chs = list()
eng = list()

try:
	with open(fullpath, "r", encoding="utf-8") as f:
		jsonfile = json.load(f)
except:
	print("Cannot parse file: " + fullpath)

for sub_jsonfile in jsonfile:
	for temp in sub_jsonfile["Files"]:
		prop.append(sub_jsonfile["Files"][temp][0])
		break
	chs.append(sub_jsonfile["Texts"]["Chs"])
	eng.append(sub_jsonfile["Texts"]["Eng"])

# os.remove(fullpath)
fullpath_patch = fullpath.replace(".json", ".patch")
patchlist = list()
for i in range(len(prop)):
	patchlist.append({"op": "replace", "path": prop[i], "value": chs[i], "origin": eng[i]})
with open(fullpath_patch, "w+", encoding="utf-8") as f:
	json.dump(patchlist, f, ensure_ascii=False, indent=4)