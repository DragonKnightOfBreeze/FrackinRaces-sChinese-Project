import os

fileList = list()
root = r"translations\species"
temp = ""

for root,dirnames,filenames in os.walk(root):
	for filename in filenames:
		fullpath = os.path.join(root,filename)
		with open(fullpath,"r",encoding="utf-8") as f:
			temp = f.read()	
		with open(fullpath+".backup","w+",encoding="utf-8") as f:
			f.write(temp)