#ifndef TEST_HAWQ_RANGER_H
#define TEST_HAWQ_RANGER_H

#include <string>
#include <pwd.h>
#include <fstream>
#include "gtest/gtest.h"
#include "lib/string_util.h"

using std::vector;
using std::string;
using hawq::test::SQLUtility;
using hawq::test::Command;

class TestHawqRanger : public ::testing::Test {
public:
	TestHawqRanger() : m_sqlnum(0) {
	}
	~TestHawqRanger() {
	}

public:
	int m_sqlnum;
};

#endif
