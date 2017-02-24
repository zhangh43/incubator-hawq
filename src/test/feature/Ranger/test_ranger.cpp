#include "gtest/gtest.h"
#include "lib/command.h"
#include "lib/sql_util.h"
#include "lib/gpfdist.h"
#include "test_ranger.h"

using std::vector;
using std::string;
using hawq::test::SQLUtility;
using hawq::test::Command;

TEST_F(TestHawqRanger, BasicTest) {
    SQLUtility util;
	hawq::test::GPfdist gpdfist(&util);
	gpdfist.init_gpfdist();

    printf("database:%s\n", util.getDbName().c_str());
    string rootPath(util.getTestRootPath());
    string initfile = hawq::test::stringFormat("Ranger/sql/init_file");
    auto cmd = hawq::test::stringFormat("ls -l %s/Ranger/splitsql/normal/*.sql | grep \"^-\" | wc -l", rootPath.c_str());
    int sql_num = std::atoi(Command::getCommandOutput(cmd).c_str());
    int writablecase = 28;
    m_sqlnum = sql_num;
    string rangerHost = RANGER_HOST;
    cmd = hawq::test::stringFormat("cp %s/Ranger/a.txt /tmp/", rootPath.c_str());
    Command::getCommandStatus(cmd);

    // clear environment
    for (int i = 1; i <= m_sqlnum; i++) {
		// delete user_num
		std::string normalusername = hawq::test::stringFormat("usertest%d", i);
		std::string superusername = hawq::test::stringFormat("usersuper%d", i);
		util.execute(hawq::test::stringFormat("drop role %s;",normalusername.c_str()), false);
		util.execute(hawq::test::stringFormat("drop role %s;",superusername.c_str()), false);
		// delete policy
		std::string cmd = hawq::test::stringFormat("ls -l %s/Ranger/policy/%d/ | grep \"^-\" | wc -l 2>/dev/null", rootPath.c_str(), i);
		int policy_num = std::atoi(Command::getCommandOutput(cmd).c_str());
		for (int j = 1; j <= policy_num; j++) {
			cmd = hawq::test::stringFormat("python %s/Ranger/rangerpolicy.py -h %s -d policy%d-%d", rootPath.c_str(), rangerHost.c_str(), i, j);
			Command::getCommandStatus(cmd);
		}
	}

    for (int i = 1; i <= sql_num; i++) {
    	// create user_num
    	std::string superusername = hawq::test::stringFormat("usersuper%d", i);;
    	std::string username = hawq::test::stringFormat("usertest%d", i);;
    	util.execute(hawq::test::stringFormat("create role %s with login createdb superuser;", superusername.c_str()),true);
    	if(i == writablecase) { //for writable external table
    		util.execute(hawq::test::stringFormat("create role %s with login createdb CREATEEXTTABLE(type='writable') CREATEROLE;", username.c_str()),true);
    	}
    	else {
    		util.execute(hawq::test::stringFormat("create role %s with login createdb CREATEEXTTABLE CREATEROLE;", username.c_str()),true);
    	}
    	cmd = hawq::test::stringFormat("python %s/Ranger/rangeruser.py -h %s -u %s,%s", rootPath.c_str(),
    			rangerHost.c_str(),username.c_str(), superusername.c_str());
		Command::getCommandStatus(cmd);

		//run sql by different users
		string normal_sqlfile = hawq::test::stringFormat("Ranger/splitsql/normal/%d.sql", i);
		string super_sqlfile = hawq::test::stringFormat("Ranger/splitsql/super/%d.sql", i);
		string admin_sqlfile = hawq::test::stringFormat("Ranger/splitsql/admin/%d.sql", i);
		string normal_ansfile_fail = hawq::test::stringFormat("Ranger/ans/normal%d_fail.ans", i);
		string super_ansfile_fail = hawq::test::stringFormat("Ranger/ans/super%d_fail.ans", i);
		string admin_ansfile = hawq::test::stringFormat("Ranger/ans/adminfirst%d.ans", i);

		cmd = hawq::test::stringFormat("ls -l %s/Ranger/policy/%d/ | grep \"^-\" | wc -l 2>/dev/null", rootPath.c_str(), i);
		int policy_num = std::atoi(Command::getCommandOutput(cmd).c_str());

		cmd = hawq::test::stringFormat("ls -l %s/Ranger/splitsql/super/%d.sql | grep \"^-\" | wc -l 2>/dev/null", rootPath.c_str(), i);
		int supersqlexist = std::atoi(Command::getCommandOutput(cmd).c_str());

		if (policy_num > 0){
			if (supersqlexist) {
				util.execSQLFile(super_sqlfile, super_ansfile_fail, initfile, true);
			}
			else {
				util.execSQLFile(normal_sqlfile, normal_ansfile_fail, initfile, true);
			}
		}

		util.execSQLFile(admin_sqlfile, admin_ansfile, initfile, true);


		for (int j = 1; j <= policy_num; j++) {
			cmd = hawq::test::stringFormat("python %s/Ranger/rangerpolicy.py -h %s -a %s/Ranger/policy/%d/%d.json", rootPath.c_str(), rangerHost.c_str(), rootPath.c_str(), i, j);
			Command::getCommandStatus(cmd);
		}
	}

	sleep(60);

    for (int i = 1; i <= sql_num; i++) {
    	//run sql by different users
		string normal_sqlfile = hawq::test::stringFormat("Ranger/splitsql/normal/%d.sql", i);
		string super_sqlfile = hawq::test::stringFormat("Ranger/splitsql/super/%d.sql", i);
		string normal_ansfile_success = hawq::test::stringFormat("Ranger/ans/normal%d_success.ans", i);
		string super_ansfile_success = hawq::test::stringFormat("Ranger/ans/super%d_success.ans", i);


		cmd = hawq::test::stringFormat("ls -l %s/Ranger/policy/%d/ | grep \"^-\" | wc -l", rootPath.c_str(), i);
		int policy_num = std::atoi(Command::getCommandOutput(cmd).c_str());
		cmd = hawq::test::stringFormat("ls -l %s/Ranger/splitsql/super/%d.sql | grep \"^-\" | wc -l", rootPath.c_str(), i);
		int supersqlexist = std::atoi(Command::getCommandOutput(cmd).c_str());
		util.execSQLFile(normal_sqlfile, normal_ansfile_success, initfile, true);
		if (supersqlexist) {
			util.execSQLFile(super_sqlfile, super_ansfile_success, initfile, true);
		}
	}

    //using gpadmin to clear database environment.
    for (int i = 1; i <= sql_num; i++) {
    	string admin_sqlfile = hawq::test::stringFormat("Ranger/splitsql/admin/%d.sql", i);
    	string admin_ansfile = hawq::test::stringFormat("Ranger/ans/adminsecond%d.ans", i);
    	util.execSQLFile(admin_sqlfile, admin_ansfile, initfile, true);
    }



    //string rootPath(util.getTestRootPath());
	//string rangerHost = RANGER_HOST;
	for (int i = 1; i <= m_sqlnum; i++) {
		// delete user_num
		std::string normalusername = hawq::test::stringFormat("usertest%d", i);
		std::string superusername = hawq::test::stringFormat("usersuper%d", i);
		util.execute(hawq::test::stringFormat("drop role %s;",normalusername.c_str()), false);
		util.execute(hawq::test::stringFormat("drop role %s;",superusername.c_str()), false);
		// delete policy
		std::string cmd = hawq::test::stringFormat("ls -l %s/Ranger/policy/%d/ | grep \"^-\" | wc -l 2>/dev/null", rootPath.c_str(), i);
		int policy_num = std::atoi(Command::getCommandOutput(cmd).c_str());
		for (int j = 1; j <= policy_num; j++) {
			cmd = hawq::test::stringFormat("python %s/Ranger/rangerpolicy.py -h %s -d policy%d-%d", rootPath.c_str(), rangerHost.c_str(), i, j);
			Command::getCommandStatus(cmd);
		}
	}


    gpdfist.finalize_gpfdist();

}
