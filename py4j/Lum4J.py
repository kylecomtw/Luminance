from py4j.java_gateway import JavaGateway
import json

class Lum4J:
    def __init__(self, index_dir):
        self.lum = JavaGateway().entry_point.GetInst(index_dir)
    
    def findWord(self, word):
        ret = self.lum.findWord(word)
        jret = self.lum.J2S(ret)
        jobj = json.loads(jret)
        return jobj

    def findGrams(self, grams):
        ret = self.lum.findGrams(word)
        jret = self.lum.J2S(ret)
        jobj = json.loads(jret)
        return jobj
    
    def add_document(self, text):
        ret = self.lum.J2S(self.lum.add_document(text))
        uuid = json.loads(ret)["uuid"]
        return uuid
    
    def get_annotation_template(self, uuid):
        ret = self.lum.J2S(self.lum.get_annotation_template(uuid))
        jobj = json.loads(ret)
        return jobj
    
    def match_text(self, inputs, pattern):
        ret = self.lum.J2S(self.lum.match_text(inputs, pattern))
        jobj = json.loads(ret)
        return jobj

    def format_kwics(self, kwic_arr):
        return [self.format_kwic(x) for x in kwic_arr] 

    def format_kwic(self, kwic):
        buf = "/".join([self.format_token(x) for x in kwic["prec"]])
        buf += "<" + 
               "/".join([self.format_token(x) for x in kwic["target"]])
               + ">"
        buf += "/".join([self.format_token(x) for x in kwic["succ"]])
        return buf
    
    def format_token(self, tok):
        if "pos" in tok:
            return "%s(%s)" % (tok["text"], tok["pos"])
        else:
            return tok["text"]
