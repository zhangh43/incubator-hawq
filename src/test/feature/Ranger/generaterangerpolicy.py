import sys
import json
import os


def get_base_policy(current_query, policy_dict, query_index, resource_index):
    basic_policy = {}
    basic_policy["allowExceptions"] = []
    basic_policy["denyExceptions"] = []
    basic_policy["denyPolicyItems"] = []
    basic_policy["name"] = "policy" + str(query_index) + "-" + str(resource_index)
    basic_policy["description"] = "no description"
    basic_policy["service"] = "hawq"
    basic_policy["isEnabled"] = True
    basic_policy["isAuditEnabled"] = True
    basic_policy["version"] = 1
    basic_policy["policyItems"] = []
    is_schema = False;
    access_array = policy_dict["access"]
    for access_item in access_array:
        policy_item={}
        policy_item["accesses"]=[]
        privilege_item_array = access_item["privileges"]
        
        basic_policy["policyItems"].append(policy_item)
        #resource item
        resource_item = access_item["resource"]
        basic_policy["resources"] = {}
        schema_exist = False
        schema_child_exist = False
        database_exist = False
        language_exist = False;
        for k,v in resource_item.iteritems():
            basic_policy["resources"][k] = {}
            basic_policy["resources"][k]["values"] = []
            basic_policy["resources"][k]["values"].append(v)
            basic_policy["resources"][k]["isExcludes"] = False
            basic_policy["resources"][k]["isRecursive"] = False
            
            if k == "database":
                database_exist = True
            if k == "schema":
                schema_exist =True
            if k == "function" or k == "table" or k =="sequence":
                schema_child_exist = True
            if k == "language":
                language_exist = True
        #if  schema_exist:
        #    basic_policy["resources"]["database"]["isExcludes"] = False
        #if  schema_child_exist:
        #    basic_policy["resources"]["database"]["isExcludes"] = False
        #    basic_policy["resources"]["schema"]["isExcludes"] = False
        if database_exist and not language_exist and not schema_exist:
            basic_policy["resources"]["schema"] = {}
            basic_policy["resources"]["schema"]["values"] = []
            basic_policy["resources"]["schema"]["values"].append("*")
            basic_policy["resources"]["schema"]["isExcludes"] = False
            basic_policy["resources"]["schema"]["isRecursive"] = False
            basic_policy["resources"]["table"] = {}
            basic_policy["resources"]["table"]["values"] = []
            basic_policy["resources"]["table"]["values"].append("*")
            basic_policy["resources"]["table"]["isExcludes"] = False
            basic_policy["resources"]["table"]["isRecursive"] = False
        if schema_exist and not schema_child_exist:
            basic_policy["resources"]["table"] = {}
            basic_policy["resources"]["table"]["values"] = []
            basic_policy["resources"]["table"]["values"].append("*")
            basic_policy["resources"]["table"]["isExcludes"] = False
            basic_policy["resources"]["table"]["isRecursive"] = False
            is_schema  = True
            
        #basic_policy["resources"]["database"]["isExcludes"] = False
        #basic_policy["resources"]["database"]["isRecursive"] = False
        #basic_policy["resources"]["database"]["values"] = ["abc"]
        #basic_policy["resources"]["language"]={}
        #basic_policy["resources"]["language"]["isExcludes"] = False
        #basic_policy["resources"]["language"]["isRecursive"] = False
        #basic_policy["resources"]["language"]["values"] = ["*"]
        
    for privilege_item in privilege_item_array:
        policy_access = {}
        policy_access["isAllowed"] = True
        policy_access["type"] = privilege_item
        if privilege_item=="create":
            policy_access["type"] = "create-schema"
        if is_schema == True and privilege_item=="usage":
            policy_access["type"] = "usage-schema"
        if is_schema == True and privilege_item=="create":
            policy_access["type"] = "create"
        policy_item["accesses"].append(policy_access);
        #policy_item["accesses"] = [{"isAllowed":True, "type": "select"}]
    policy_item["conditions"] = []
    policy_item["delegateAdmin"] = True
    policy_item["groups"] = None #["public"]
    uname =[]
    uname.append("usertest"+str(query_index));
    uname.append("usersuper"+str(query_index));
    policy_item["users"] = uname
    print basic_policy
    print json.dumps(basic_policy, sort_keys=True)
    output_dir_name = "/tmp/policy/{}".format(\
                        query_index)
    output_file_name = "/tmp/policy/{}/{}.json".format(\
                        query_index,resource_index)
    if not os.path.exists(output_dir_name):
        os.makedirs(output_dir_name)
    jsonfile = open(output_file_name, "w")
    jsonfile.write(json.dumps(basic_policy, sort_keys=True))
    
    
def prepare_generate_json_file(policy_json_file_name):
    inputfile = open(policy_json_file_name, "r")
    current_query=""
    query_index = 0;
    for line in inputfile:
        line=line.strip()
        if line.startswith("Current Query is:"):
            current_query = line[17:]
            query_index = query_index + 1
            resource_index = 0;
            output_dir_name = "/tmp/policy/{}".format(\
                        query_index)
            if not os.path.exists(output_dir_name):
                os.makedirs(output_dir_name)
        if line.startswith("{"):
            #print line["access"]
            policy_dict=json.loads(line)
            resource_index = resource_index + 1
            get_base_policy(current_query, policy_dict, query_index,resource_index)
            #policyname = jsonobject['name']
            #print jsonobject
        #print line

if __name__ == '__main__':
    policy_json_file_name = "/Users/huanzhang/Downloads/policyinput.txt"
    prepare_generate_json_file(policy_json_file_name)
    print "Generate Policies to a folder."
    #basic_policy = get_base_policy()
    