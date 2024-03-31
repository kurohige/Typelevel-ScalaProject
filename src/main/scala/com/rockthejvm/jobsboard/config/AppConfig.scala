package com.rockthejvm.jobsboard.config

import pureconfig.ConfigReader
import pureconfig.generic.derivation.default.*

final case class AppConfig(
    postgres: PostgresConfig,
    emberConfig: EmberConfig
) derives ConfigReader
