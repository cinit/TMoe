//
// Created by kinit on 2021-10-11.
//

#include <sys/poll.h>
#include <cerrno>

#include "io_utils.h"

int wait_for_input(int fd, int timeout_ms) {
    struct pollfd pollFd = {.fd = fd, .events = POLLIN};
    do {
        int index = poll(&pollFd, 1, timeout_ms);
        if (index == 1) {
            return 1;
        } else if (errno == 0) {
            return 0;
        } else {
            if (errno == EINTR) {
                continue;
            } else {
                return -errno;
            }
        }
    } while (true);
}
