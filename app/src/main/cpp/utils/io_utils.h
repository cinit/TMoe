//
// Created by kinit on 2021-10-11.
//

#ifndef NCICLIENT_IO_UTILS_H
#define NCICLIENT_IO_UTILS_H

#include <unistd.h>
#include <cstddef>

/**
 * Wait for input data to be ready.
 * @param fd fd to wait for data input
 * @param timeout_ms max time to wait in ms
 * @return 1 if input data available, 0 if timeout, -errno if errors
 */
int wait_for_input(int fd, int timeout_ms);

#endif //NCICLIENT_IO_UTILS_H
